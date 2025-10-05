package com.aurionpro.app.service;

import com.aurionpro.app.dto.UserCreateRequest;
import com.aurionpro.app.dto.UserResponse;
import com.aurionpro.app.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    UserResponse createUser(UserCreateRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long userId);
    UserResponse updateUser(Long userId, UserUpdateRequest request);

    void softDeleteUser(Long userId);
    Page<UserResponse> listInactiveUsers(Pageable pageable);
    UserResponse restoreUser(Long userId);
    void hardDeleteUser(Long userId);
}