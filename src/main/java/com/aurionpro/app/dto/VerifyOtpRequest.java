package com.aurionpro.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "A valid email address is required.")
    private String email;
    @NotBlank(message = "OTP is required.")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits.")
    private String otp;
}