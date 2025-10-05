package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.AuthFailedException;
import com.aurionpro.app.common.exception.InvalidOtpException;
import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.*;
import com.aurionpro.app.entity.*;
import com.aurionpro.app.repository.PendingRegistrationRepository;
import com.aurionpro.app.repository.RefreshTokenRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import com.aurionpro.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final Random random;

    @Override
    public void register(SignupRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists.");
        }
        
        pendingRegistrationRepository.findByEmail(request.getEmail()).ifPresent(pendingRegistrationRepository::delete);
        
        String otp = String.valueOf(100000 + random.nextInt(900000));
        
        PendingRegistration pending = PendingRegistration.builder()
                .email(request.getEmail())
                .name(request.getName())
                .gender(request.getGender())
                .contactNumber(request.getContactNumber())
                .otp(otp)
                .expiryDate(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();
        pendingRegistrationRepository.save(pending);
        
        otpService.sendOtpEmail(request.getEmail(), otp);
        
        log.info("Pending registration for [{}] created. Verification OTP sent.", request.getEmail());
    }

    @Override
    public AuthResponse verifyRegistration(VerifyOtpRequest request) {
        log.info("Verifying registration for email: {}", request.getEmail());

        PendingRegistration pending = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidOtpException("Invalid registration request or session expired."));

        if (Instant.now().isAfter(pending.getExpiryDate())) {
            pendingRegistrationRepository.delete(pending);
            throw new InvalidOtpException("OTP has expired. Please register again.");
        }

        if (!pending.getOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("The OTP you entered is incorrect.");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default ROLE_USER not found."));

        User newUser = User.builder()
                .name(pending.getName())
                .gender(pending.getGender())
                .email(pending.getEmail())
                .phoneNumber(pending.getContactNumber())
                .roles(Collections.singleton(userRole))
                .enabled(true)
                .build();
        userRepository.save(newUser);

        pendingRegistrationRepository.delete(pending);
        
        log.info("User account for [{}] successfully created and enabled.", request.getEmail());
        return jwtService.generateTokens(request.getEmail());
    }

    @Override
    public AuthResponse verifyLogin(VerifyOtpRequest request) {
        log.info("Verifying OTP login for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new AuthFailedException("User account is not enabled. Please complete registration or contact support.");
        }
        
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        
        return jwtService.generateTokens(request.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Authentication attempt for user [{}]", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        boolean isPrivilegedUser = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN || role.getName() == RoleName.ROLE_STAFF);

        if (!isPrivilegedUser) {
            log.warn("Security Violation: Customer user [{}] attempted password-based login.", loginRequest.getEmail());
            throw new AccessDeniedException("Access Denied: This login method is for admins and staff only.");
        }

        return jwtService.generateTokens(authentication.getName());
    }

    @Override
    public void logout(String refreshToken) {
        log.info("Processing logout for a refresh token.");
        try {
            String email = jwtService.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found during logout"));
            
            for (RefreshToken token : refreshTokenRepository.findByUser(user)) {
                if (passwordEncoder.matches(refreshToken, token.getTokenHash())) {
                    refreshTokenRepository.delete(token);
                    log.info("Successfully deleted refresh token for user [{}]", email);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Forgot password request for user [{}]", email);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN || r.getName() == RoleName.ROLE_STAFF)) {
            log.warn("Password reset attempt for non-privileged or non-existent user [{}]. No action taken for security.", email);
            return; 
        }
        otpService.generateAndSendLoginOtp(email);
        log.info("Password reset OTP sent to privileged user [{}]", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt for user [{}]", request.getEmail());
        
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> 
            new ResourceNotFoundException("User not found with email: " + request.getEmail()));
            
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully reset for user [{}]", request.getEmail());
    }
}
