package com.aurionpro.app.security;

import com.aurionpro.app.dto.AuthResponse;
import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.Role;
import com.aurionpro.app.entity.RoleName;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.repository.RefreshTokenRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    @Transactional
    public AuthResponse generateTokens(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("No existing user for [{}]. Creating new user.", email);
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("FATAL: Default ROLE_USER not found."));
            return userRepository.save(User.builder().email(email).roles(Collections.singleton(userRole)).build());
        });
        
        String accessToken = buildToken(user.getEmail(), jwtProperties.getAccessTokenMs());
        String refreshTokenString = buildToken(user.getEmail(), jwtProperties.getRefreshTokenMs());
        
        String refreshTokenHash = passwordEncoder.encode(refreshTokenString);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(refreshTokenHash)
                .expiryDate(getExpirationDate(refreshTokenString).toInstant())
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken);
        
        return new AuthResponse(accessToken, refreshTokenString);
    }
    
    @Override
    public Optional<String> refreshAccessToken(String refreshToken) {
        try {
            if (isTokenExpired(refreshToken)) return Optional.empty();
            
            String email = extractEmail(refreshToken);
            User user = userRepository.findByEmail(email).orElseThrow();
            
            boolean isMatch = refreshTokenRepository.findByUser(user)
                    .stream()
                    .anyMatch(rt -> passwordEncoder.matches(refreshToken, rt.getTokenHash()) && rt.getExpiryDate().isAfter(Instant.now()));

            if (isMatch) {
                return Optional.of(buildToken(email, jwtProperties.getAccessTokenMs()));
            }
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(email)) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Date getExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public String generateQrPayload(Ticket ticket) {
        Instant expiry = ticket.getValidUntil() != null ? ticket.getValidUntil() : ticket.getCreatedAt().plus(24, ChronoUnit.HOURS);
        return Jwts.builder()
                .claim("ticketId", ticket.getId())
                .claim("userId", ticket.getUser().getId())
                .claim("type", ticket.getTicketType().name())
                .issuedAt(Date.from(ticket.getCreatedAt()))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    private String buildToken(String email, long expirationMs) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}