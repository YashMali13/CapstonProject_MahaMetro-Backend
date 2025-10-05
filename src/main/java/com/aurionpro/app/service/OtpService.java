package com.aurionpro.app.service;

public interface OtpService {
    void generateAndSendLoginOtp(String email); 
    void sendOtpEmail(String email, String otp); 
    void verifyOtp(String email, String otp);
}