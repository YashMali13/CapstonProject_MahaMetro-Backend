package com.aurionpro.app.dto;

import com.aurionpro.app.entity.RoleName;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    private String name;
    private String phoneNumber;
    private Set<RoleName> roles;
}