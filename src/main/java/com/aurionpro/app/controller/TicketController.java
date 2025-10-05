package com.aurionpro.app.controller;

import com.aurionpro.app.dto.BookTicketRequest;
import com.aurionpro.app.dto.BookingInitiationResponse;
import com.aurionpro.app.dto.TicketResponse;
import com.aurionpro.app.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/book-with-upi")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BookingInitiationResponse> bookTicketWithUpi(@Valid @RequestBody BookTicketRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(ticketService.initiateUpiBooking(request, userEmail));
    }
    
    @PostMapping("/book-with-wallet")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<TicketResponse>> bookTicketWithWallet(@Valid @RequestBody BookTicketRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(ticketService.bookTicketWithWallet(request, userEmail));
    }

    @GetMapping(value = "/{ticketId}/view", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> viewTicketQrCode(@PathVariable Long ticketId, Authentication authentication) throws IOException {
        String userEmail = authentication.getName();
        byte[] qrCodeImage = ticketService.downloadTicket(ticketId, userEmail);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);
    }

    @GetMapping(value = "/{ticketId}/download", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long ticketId, Authentication authentication) throws IOException {
        String userEmail = authentication.getName();
        byte[] qrCodeImage = ticketService.downloadTicket(ticketId, userEmail);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "ticket-qr-" + ticketId + ".png");
        return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
    }

    @GetMapping("/{ticketId}/payload")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> getTicketQrPayload(@PathVariable Long ticketId, Authentication authentication) {
        String userEmail = authentication.getName();
        String payload = ticketService.getTicketQrPayload(ticketId, userEmail);
        return ResponseEntity.ok(Map.of("qrPayload", payload));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<TicketResponse>> getTicketHistory(Authentication authentication, @ParameterObject Pageable pageable) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(ticketService.getTicketHistory(userEmail, pageable));
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TicketResponse> getTicketDetails(@PathVariable Long ticketId, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(ticketService.getTicketDetails(ticketId, userEmail));
    }

    @PostMapping("/{ticketId}/cancel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TicketResponse> cancelTicket(@PathVariable Long ticketId, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(ticketService.cancelTicket(ticketId, userEmail));
    }
}