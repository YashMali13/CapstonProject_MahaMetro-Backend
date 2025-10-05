package com.aurionpro.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank @Email
    private String email;
    @NotBlank(message = "OTP is required.")
    private String otp;
    @NotBlank
    @Size(min = 8, message = "New password must be at least 8 characters long.")
    private String newPassword;
}