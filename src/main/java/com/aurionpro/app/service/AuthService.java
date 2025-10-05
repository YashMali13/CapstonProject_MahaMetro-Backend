package com.aurionpro.app.service;

import com.aurionpro.app.dto.AuthResponse;
import com.aurionpro.app.dto.LoginRequest;
import com.aurionpro.app.dto.ResetPasswordRequest;
import com.aurionpro.app.dto.SignupRequest;
import com.aurionpro.app.dto.VerifyOtpRequest;

public interface AuthService {

    void register(SignupRequest request);
    AuthResponse verifyRegistration(VerifyOtpRequest request);

    AuthResponse verifyLogin(VerifyOtpRequest request);

    AuthResponse login(LoginRequest loginRequest);
    void logout(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
}