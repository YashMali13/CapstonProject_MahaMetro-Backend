package com.aurionpro.app.dto;

import com.aurionpro.app.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
    private String name;

    @NotNull(message = "Gender is required.")
    private Gender gender;

    @NotBlank(message = "Email is required.")
    @Email(message = "A valid email address is required.")
    private String email;

    @NotBlank(message = "Contact number is required.")
    @Size(min = 10, max = 15, message = "Contact number must be between 10 and 15 digits.")
    private String contactNumber;
}