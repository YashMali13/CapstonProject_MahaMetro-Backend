package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.TransactionHistoryResponse;
import com.aurionpro.app.entity.PaymentMethod;
import com.aurionpro.app.entity.PaymentStatus;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.WalletTransaction;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TicketRepository ticketRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Override
    public Page<TransactionHistoryResponse> getTransactionHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        
        List<Ticket> tickets = ticketRepository.findByUser(user);
        List<WalletTransaction> walletTransactions = walletTransactionRepository.findByWallet_User(user);

        List<TransactionHistoryResponse> combinedList = new ArrayList<>();

        for (Ticket t : tickets) {
            combinedList.add(TransactionHistoryResponse.builder()
                    .transactionId("TICKET-" + t.getId())
                    .description(t.getOriginStation().getName() + " to " + t.getDestinationStation().getName())
                    .timestamp(t.getCreatedAt())
                    .amount(t.getFare().negate()) 
                    .type("DEBIT")
                    .status(t.getPayment().getStatus())
                    .paymentMethod(t.getPayment().getMethod())
                    .build());
        }

        for (WalletTransaction wt : walletTransactions) {
             combinedList.add(TransactionHistoryResponse.builder()
                    .transactionId("WALLET-" + wt.getId())
                    .description(wt.getDescription())
                    .timestamp(wt.getCreatedAt())
                    .amount(wt.getAmount()) 
                    .type(wt.getType().name())
                    .status(wt.getPayment() != null ? wt.getPayment().getStatus() : PaymentStatus.PAID)
                    .paymentMethod(wt.getPayment() != null ? wt.getPayment().getMethod() : PaymentMethod.WALLET)
                    .build());
        }
        
        combinedList.sort(Comparator.comparing(TransactionHistoryResponse::getTimestamp).reversed());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), combinedList.size());
        List<TransactionHistoryResponse> pageContent = (start <= end) ? combinedList.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, combinedList.size());
    }
}