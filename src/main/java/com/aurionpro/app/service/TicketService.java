package com.aurionpro.app.service;

import com.aurionpro.app.dto.BookTicketRequest;
import com.aurionpro.app.dto.BookingInitiationResponse;
import com.aurionpro.app.dto.PaymentConfirmationRequest;
import com.aurionpro.app.dto.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;

public interface TicketService {

    BookingInitiationResponse initiateUpiBooking(BookTicketRequest request, String userEmail);

    List<TicketResponse> bookTicketWithWallet(BookTicketRequest request, String userEmail);
    
    List<TicketResponse> confirmBooking(PaymentConfirmationRequest confirmationRequest);
    
    Page<TicketResponse> getTicketHistory(String userEmail, Pageable pageable);
    
    TicketResponse getTicketDetails(Long ticketId, String userEmail);
    
    TicketResponse cancelTicket(Long ticketId, String userEmail);
    
    byte[] downloadTicket(Long ticketId, String userEmail) throws IOException;

    // --- NEW METHOD ---
    String getTicketQrPayload(Long ticketId, String userEmail);
}