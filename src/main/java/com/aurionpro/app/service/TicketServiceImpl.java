package com.aurionpro.app.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.exception.InsufficientWalletBalanceException;
import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.common.exception.TicketBookingException;
import com.aurionpro.app.dto.BookTicketRequest;
import com.aurionpro.app.dto.BookingInitiationResponse;
import com.aurionpro.app.dto.FareRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.PaymentConfirmationRequest;
import com.aurionpro.app.dto.TicketResponse;
import com.aurionpro.app.entity.ActionType;
import com.aurionpro.app.entity.ActivityLog;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.TicketStatus;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.entity.WalletTransactionType;
import com.aurionpro.app.mapper.TicketMapper;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import com.aurionpro.app.security.JwtService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FareService fareService;
    private final JwtService jwtService;
    private final TicketMapper ticketMapper;
    private final RazorpayService razorpayService;
    private final StationRepository stationRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final QrCodeService qrCodeService;
    private final WalletService walletService;
    private final ActivityLogService activityLogService;

    @Override
    public BookingInitiationResponse initiateUpiBooking(BookTicketRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // --- CORRECTED LOGIC ---
        // 1. Create the FareRequest DTO to encapsulate all inputs.
        FareRequest fareRequest = new FareRequest();
        fareRequest.setOriginStationId(request.getOriginStationId());
        fareRequest.setDestinationStationId(request.getDestinationStationId());
        fareRequest.setTicketType(request.getTicketType());
        fareRequest.setPassengerCount(request.getPassengerCount());

        // 2. Call the new, cleaner service method.
        FareResponse fareDetails = fareService.calculateFare(fareRequest);
        BigDecimal totalFare = fareDetails.getTotalFare(); // 3. Use the total fare from the response.

        Payment payment = Payment.builder()
                .user(user).amount(totalFare).currency("INR")
                .method(PaymentMethod.UPI).status(PaymentStatus.CREATED)
                .ticketType(request.getTicketType())
                .originStationId(request.getOriginStationId())
                .destinationStationId(request.getDestinationStationId())
                .passengerCount(request.getPassengerCount()).build();
        paymentRepository.save(payment);

        try {
            Order razorpayOrder = razorpayService.createOrder(totalFare);
            String razorpayOrderId = razorpayOrder.get("id");
            payment.setProviderOrderId(razorpayOrderId);
            paymentRepository.save(payment);

            log.info("Razorpay order {} created for user [{}] for {} passengers",
                    razorpayOrderId, userEmail, request.getPassengerCount());
            return BookingInitiationResponse.builder()
                    .internalPaymentId(payment.getId())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(totalFare)
                    .currency("INR")
                    .build();
        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            activityLogService.log(ActionType.TICKET_BOOKED, user.getEmail(), ActivityLog.LogLevel.ERROR,
                    "Razorpay order creation failed: " + e.getMessage(), String.valueOf(payment.getId()));
            log.error("Razorpay error for user [{}], paymentId [{}]. Marked as FAILED.", userEmail, payment.getId(), e);
            throw new TicketBookingException("Could not initiate payment with provider. Please try again later.");
        }
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public List<TicketResponse> bookTicketWithWallet(BookTicketRequest request, String userEmail) {
        log.info("Attempting wallet booking for user [{}]", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // --- CORRECTED LOGIC ---
        FareRequest fareRequest = new FareRequest();
        fareRequest.setOriginStationId(request.getOriginStationId());
        fareRequest.setDestinationStationId(request.getDestinationStationId());
        fareRequest.setTicketType(request.getTicketType());
        fareRequest.setPassengerCount(request.getPassengerCount());

        FareResponse fareDetails = fareService.calculateFare(fareRequest);
        BigDecimal totalFare = fareDetails.getTotalFare();

        Wallet wallet = walletRepository.findForUpdateByUser_Email(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userEmail));

        Payment payment = Payment.builder()
                .user(user).amount(totalFare).currency("INR")
                .method(PaymentMethod.WALLET).status(PaymentStatus.CREATED)
                .ticketType(request.getTicketType())
                .originStationId(request.getOriginStationId())
                .destinationStationId(request.getDestinationStationId())
                .passengerCount(request.getPassengerCount()).build();
        paymentRepository.save(payment);

        if (wallet.getBalance().compareTo(totalFare) < 0) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            activityLogService.log(ActionType.TICKET_BOOKED, user.getEmail(), ActivityLog.LogLevel.WARN,
                    "Insufficient wallet balance.", String.valueOf(payment.getId()));
            throw new InsufficientWalletBalanceException("Insufficient wallet balance. Please recharge.");
        }

        wallet.setBalance(wallet.getBalance().subtract(totalFare));
        walletRepository.save(wallet);

        payment.setStatus(PaymentStatus.PAID);
        Payment savedPayment = paymentRepository.save(payment);

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet).payment(savedPayment).amount(totalFare.negate())
                .type(WalletTransactionType.DEBIT)
                .description(request.getPassengerCount() + " Ticket(s) Purchase (paymentId=" + savedPayment.getId() + ")")
                .build();
        walletTransactionRepository.save(tx);

        return createFinalTickets(savedPayment, fareDetails);
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public List<TicketResponse> confirmBooking(PaymentConfirmationRequest confirmationRequest) {
        Payment payment = paymentRepository.findByProviderOrderIdForUpdate(confirmationRequest.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + confirmationRequest.getRazorpayOrderId()));

        if (ticketRepository.existsByPayment_Id(payment.getId())) {
            log.warn("Ticket(s) for paymentId: {} already exist. Skipping duplicate creation.", payment.getId());
            List<Ticket> existing = ticketRepository.findByPayment_Id(payment.getId());
            return existing.stream().map(ticketMapper::toResponse).collect(Collectors.toList());
        }

        payment.setProviderPaymentId(confirmationRequest.getRazorpayPaymentId());
        payment.setProviderSignature(confirmationRequest.getRazorpaySignature());

        // --- CORRECTED LOGIC ---
        FareRequest fareRequest = new FareRequest();
        fareRequest.setOriginStationId(payment.getOriginStationId());
        fareRequest.setDestinationStationId(payment.getDestinationStationId());
        fareRequest.setTicketType(payment.getTicketType());
        fareRequest.setPassengerCount(payment.getPassengerCount());

        FareResponse fareDetails = fareService.calculateFare(fareRequest);

        List<TicketResponse> created = createFinalTickets(payment, fareDetails);

        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        log.info("Payment {} marked PAID and {} ticket(s) created for order {}.",
                payment.getId(), created.size(), confirmationRequest.getRazorpayOrderId());
        return created;
    }

    private List<TicketResponse> createFinalTickets(Payment payment, FareResponse fareDetails) {
        List<Ticket> ticketsToSave = new ArrayList<>();
        String bookingGroupId = UUID.randomUUID().toString();

        int passengerCount = (payment.getPassengerCount() != null && payment.getPassengerCount() > 0)
                ? payment.getPassengerCount() : 1;

        Station origin = payment.getOriginStationId() != null
                ? stationRepository.findById(payment.getOriginStationId()).orElse(null) : null;
        Station destination = payment.getDestinationStationId() != null
                ? stationRepository.findById(payment.getDestinationStationId()).orElse(null) : null;

        for (int i = 0; i < passengerCount; i++) {
            Ticket ticket = Ticket.builder()
                    .bookingGroupId(bookingGroupId)
                    .user(payment.getUser()).payment(payment)
                    .originStation(origin).destinationStation(destination)
                    .ticketType(payment.getTicketType())
                    .fare(fareDetails.getSinglePassengerFare()) // Use the single fare for each ticket
                    .status(TicketStatus.NEW)
                    .journeysCompleted(0)
                    .refundProcessed(false)
                    .build();

            if (fareDetails.getDurationInDays() != null) {
                ticket.setValidFrom(Instant.now());
                ticket.setValidUntil(Instant.now().plus(fareDetails.getDurationInDays(), ChronoUnit.DAYS));
            }
            if (fareDetails.getTotalTrips() != null) {
                ticket.setTotalTrips(fareDetails.getTotalTrips());
                ticket.setRemainingTrips(fareDetails.getTotalTrips());
            }
            ticketsToSave.add(ticket);
        }

        List<Ticket> saved = ticketRepository.saveAll(ticketsToSave);
        for (Ticket t : saved) {
            String qrPayload = jwtService.generateQrPayload(t);
            t.setQrPayload(qrPayload);
        }
        List<Ticket> finalTickets = ticketRepository.saveAll(saved);

        activityLogService.log(ActionType.TICKET_BOOKED, payment.getUser().getEmail(), ActivityLog.LogLevel.INFO,
                passengerCount + " ticket(s) booked via " + payment.getMethod().name(), bookingGroupId);
        log.info("Successfully created {} ticket(s) with bookingGroupId: {}", finalTickets.size(), bookingGroupId);

        return finalTickets.stream().map(ticketMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketHistory(String userEmail, Pageable pageable) {
        log.info("Fetching ticket history for user [{}]", userEmail);
        return ticketRepository.findAllByUser_EmailOrderByCreatedAtDesc(userEmail, pageable)
                .map(ticketMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketDetails(Long ticketId, String userEmail) {
        log.info("Fetching details for ticket id: {}", ticketId);
        return ticketRepository.findByIdAndUser_Email(ticketId, userEmail)
                .map(ticketMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
    }

    @Override
    @Retryable(retryFor = {ObjectOptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TicketResponse cancelTicket(Long ticketId, String userEmail) {
        log.info("User [{}] attempting to cancel ticket id: {}", userEmail, ticketId);

        Ticket ticket = ticketRepository.findByIdAndUser_Email(ticketId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.isRefundProcessed()) {
            throw new TicketBookingException("Refund has already been processed for this specific ticket.");
        }

        if (ticket.getStatus() != TicketStatus.NEW) {
            throw new TicketBookingException("Only NEW, unused tickets can be cancelled. Current status: " + ticket.getStatus());
        }
        if (ticket.getValidFrom() != null && Instant.now().isAfter(ticket.getValidFrom())) {
            throw new TicketBookingException("Ticket is no longer valid for cancellation as the travel time has started.");
        }

        Payment payment = paymentRepository.findByIdForUpdate(ticket.getPayment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ticket: " + ticketId));

        try {
            activityLogService.log(ActionType.WALLET_REFUND_ISSUED, payment.getUser().getEmail(), ActivityLog.LogLevel.INFO,
                    "Attempting refund for ticket " + ticketId, String.valueOf(payment.getId()));

            if (payment.getMethod() == PaymentMethod.WALLET) {
                walletService.processPartialRefund(payment, ticket.getFare());
                log.info("Wallet refund of {} processed for payment id: {}", ticket.getFare(), payment.getId());
            } else if (payment.getMethod() == PaymentMethod.UPI) {
                razorpayService.createRefund(payment, ticket.getFare());
                log.info("UPI (Razorpay) refund of {} processed for payment id: {}", ticket.getFare(), payment.getId());
            } else {
                throw new TicketBookingException("Unsupported payment method for refund: " + payment.getMethod());
            }

        } catch (Exception e) {
            log.error("Refund failed for paymentId {} while cancelling ticket {}: {}", payment.getId(), ticketId, e.getMessage(), e);
            activityLogService.log(ActionType.WALLET_REFUND_ISSUED, payment.getUser().getEmail(), ActivityLog.LogLevel.ERROR,
                    "Refund failed for ticket " + ticketId + ": " + e.getMessage(), String.valueOf(payment.getId()));
            throw new TicketBookingException("Refund failed: " + e.getMessage());
        }

        ticket.setRefundProcessed(true);
        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket cancelled = ticketRepository.save(ticket);

        activityLogService.log(ActionType.TICKET_CANCELLED, ticket.getUser().getEmail(), ActivityLog.LogLevel.INFO,
                "Ticket cancelled.", String.valueOf(ticketId));
        log.info("Ticket {} cancelled successfully for user {}", ticketId, userEmail);

        return ticketMapper.toResponse(cancelled);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadTicket(Long ticketId, String userEmail) throws IOException {
        log.info("User [{}] requesting to download ticket id: {}", userEmail, ticketId);
        Ticket ticket = ticketRepository.findByIdAndUser_Email(ticketId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        if (ticket.getQrPayload() == null || ticket.getQrPayload().isEmpty()) {
            log.error("Ticket {} has no QR payload.", ticketId);
            throw new IllegalStateException("QR code has not been generated for this ticket.");
        }
        return qrCodeService.generateQrCodeImage(ticket.getQrPayload());
    }

    @Override
    @Transactional(readOnly = true)
    public String getTicketQrPayload(Long ticketId, String userEmail) {
        log.info("User [{}] requesting QR payload for ticket id: {}", userEmail, ticketId);
        Ticket ticket = ticketRepository.findByIdAndUser_Email(ticketId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getQrPayload() == null || ticket.getQrPayload().isEmpty()) {
            log.error("Ticket {} has no QR payload.", ticketId);
            throw new IllegalStateException("QR code has not been generated for this ticket.");
        }
        return ticket.getQrPayload();
    }
}
