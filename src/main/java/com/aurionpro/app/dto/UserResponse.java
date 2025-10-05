package com.aurionpro.app.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean deleted;
    private Instant deletedAt;
}