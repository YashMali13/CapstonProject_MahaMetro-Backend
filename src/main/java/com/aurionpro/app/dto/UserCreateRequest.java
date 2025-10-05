package com.aurionpro.app.dto;

import com.aurionpro.app.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class UserCreateRequest {
    @NotBlank(message = "Name is required.")
    private String name;
    @NotBlank(message = "Email is required.")
    @Email(message = "A valid email is required.")
    private String email;
    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;
    @NotEmpty(message = "At least one role is required.")
    private Set<RoleName> roles;
}