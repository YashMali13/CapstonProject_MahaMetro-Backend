package com.aurionpro.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Set;

@Data
public class ProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String profileImageUrl;
    private Set<String> roles;
    private Instant createdAt;
}