package com.aurionpro.app.security;

import com.aurionpro.app.dto.AuthResponse;
import com.aurionpro.app.entity.Ticket;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.Optional;

public interface JwtService {
    AuthResponse generateTokens(String email);
    String extractEmail(String token);
    boolean isTokenValid(String token, String email);
    Date getExpirationDate(String token);
    Optional<String> refreshAccessToken(String refreshToken);
    String generateQrPayload(Ticket ticket);
    Claims extractAllClaims(String token);
}