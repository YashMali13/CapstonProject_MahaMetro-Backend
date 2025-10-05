package com.aurionpro.app.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.common.exception.WalletOperationException;
import com.aurionpro.app.dto.BookingInitiationResponse;
import com.aurionpro.app.dto.PaymentConfirmationRequest;
import com.aurionpro.app.dto.WalletRechargeRequest;
import com.aurionpro.app.dto.WalletResponse;
import com.aurionpro.app.dto.WalletSummaryResponse;
import com.aurionpro.app.dto.WalletTransactionResponse;
import com.aurionpro.app.entity.ActionType;
import com.aurionpro.app.entity.ActivityLog;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.entity.WalletTransactionType;
import com.aurionpro.app.mapper.WalletMapper;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletMapper walletMapper;
    private final RazorpayService razorpayService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public WalletSummaryResponse getWalletForUser(String userEmail) {
        Wallet wallet = findOrCreateWalletByUserEmail(userEmail);
        return walletMapper.toSummaryResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getWalletTransactions(String userEmail, Pageable pageable) {
        Wallet wallet = findOrCreateWalletByUserEmail(userEmail);
        return walletTransactionRepository.findByWallet_IdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(walletMapper::toTransactionResponse);
    }

    @Override
    public BookingInitiationResponse initiateWalletRecharge(WalletRechargeRequest request, String userEmail) {
        log.info("User [{}] initiating wallet recharge of amount: {}", userEmail, request.getAmount());
        Wallet wallet = findOrCreateWalletByUserEmail(userEmail);

        Payment payment = Payment.builder()
                .user(wallet.getUser())
                .amount(request.getAmount())
                .currency("INR")
                .method(PaymentMethod.UPI) 
                .status(PaymentStatus.CREATED)
                .ticketType(null)
                .build();
        paymentRepository.save(payment);

        try {
            Order razorpayOrder = razorpayService.createOrder(request.getAmount());
            String razorpayOrderId = razorpayOrder.get("id");
            payment.setProviderOrderId(razorpayOrderId);
            paymentRepository.save(payment);

            log.info("Razorpay order {} created for user [{}] wallet recharge", razorpayOrderId, userEmail);
            return BookingInitiationResponse.builder()
                    .internalPaymentId(payment.getId())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .currency("INR")
                    .build();
        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            activityLogService.log(ActionType.WALLET_RECHARGE_INITIATED, userEmail, ActivityLog.LogLevel.ERROR,
                    "Razorpay order creation failed: " + e.getMessage(), String.valueOf(payment.getId()));
            log.error("Razorpay error for wallet recharge for user [{}], paymentId [{}]. Marked as FAILED.",
                    userEmail, payment.getId(), e);
            throw new WalletOperationException("Could not initiate wallet recharge. Please try again later.");
        }
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public WalletResponse confirmWalletRecharge(PaymentConfirmationRequest confirmationRequest) {
        log.info("Confirming wallet recharge for orderId: {}", confirmationRequest.getRazorpayOrderId());
        Payment payment = paymentRepository.findByProviderOrderIdForUpdate(confirmationRequest.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + confirmationRequest.getRazorpayOrderId()));

        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.warn("Wallet recharge for orderId: {} already processed. Status: {}",
                    confirmationRequest.getRazorpayOrderId(), payment.getStatus());
            Wallet wallet = findOrCreateWalletByUserEmail(payment.getUser().getEmail());
            return walletMapper.toResponse(wallet);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setProviderPaymentId(confirmationRequest.getRazorpayPaymentId());
        payment.setProviderSignature(confirmationRequest.getRazorpaySignature());
        paymentRepository.save(payment);

        Wallet wallet = walletRepository.findForUpdateByUser_Email(payment.getUser().getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for user: " + payment.getUser().getEmail()));

        wallet.setBalance(wallet.getBalance().add(payment.getAmount()));

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet).payment(payment).amount(payment.getAmount())
                .type(WalletTransactionType.CREDIT)
                .description("Wallet recharge via UPI")
                .build();
        walletTransactionRepository.save(tx);

        Wallet updated = walletRepository.save(wallet);

        activityLogService.log(ActionType.WALLET_RECHARGE_COMPLETED, payment.getUser().getEmail(),
                ActivityLog.LogLevel.INFO, "Wallet credited with " + payment.getAmount(), String.valueOf(payment.getId()));

        log.info("Wallet for user [{}] credited. New balance: {}", payment.getUser().getEmail(), updated.getBalance());
        return walletMapper.toResponse(updated);
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void processRefundForPayment(Payment payment) {
        log.info("Processing full refund for paymentId: {}", payment.getId());
        processPartialRefund(payment, payment.getAmount());
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void processPartialRefund(Payment payment, BigDecimal refundAmount) {
        log.info("Processing partial refund of {} for paymentId: {}", refundAmount, payment.getId());

        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletOperationException("Invalid refund amount. Must be positive.");
        }

        if (payment.getMethod() != PaymentMethod.WALLET) {
            throw new WalletOperationException("Wallet refund not applicable for payment method: " + payment.getMethod());
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new WalletOperationException("Payment already fully refunded.");
        }

        final BigDecimal alreadyRefunded = payment.getAmountRefunded() == null
                ? BigDecimal.ZERO
                : payment.getAmountRefunded();

        if (payment.getStatus() != PaymentStatus.PAID && alreadyRefunded.compareTo(BigDecimal.ZERO) == 0) {
            throw new WalletOperationException("Refund allowed only for PAID/partially refunded payments. Current: " + payment.getStatus());
        }

        final BigDecimal newTotalRefunded = alreadyRefunded.add(refundAmount);
        if (newTotalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new WalletOperationException("Refund amount exceeds original payment.");
        }

        Wallet wallet = walletRepository.findForUpdateByUser_Email(payment.getUser().getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + payment.getUser().getEmail()));
        wallet.setBalance(wallet.getBalance().add(refundAmount));
        walletRepository.save(wallet);

        payment.setAmountRefunded(newTotalRefunded);
        if (newTotalRefunded.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }
        paymentRepository.save(payment);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet).payment(payment).amount(refundAmount)
                .type(WalletTransactionType.CREDIT)
                .description("Refund for cancelled ticket (Payment ID: " + payment.getId() + ")")
                .build();
        walletTransactionRepository.save(tx);

        log.info("Wallet credited for refund. Payment {} refunded so far: {}. New wallet balance: {}",
                payment.getId(), newTotalRefunded, wallet.getBalance());
    }

    private Wallet findOrCreateWalletByUserEmail(String userEmail) {
        return walletRepository.findByUser_Email(userEmail)
                .orElseGet(() -> {
                    log.info("No wallet found for user [{}], creating a new one.", userEmail);
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
                    Wallet newWallet = Wallet.builder()
                            .user(user).balance(BigDecimal.ZERO).currency("INR").build();
                    return walletRepository.save(newWallet);
                });
    }
}
