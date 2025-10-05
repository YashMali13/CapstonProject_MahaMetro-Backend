package com.aurionpro.app.controller;

import com.aurionpro.app.dto.UserCreateRequest;
import com.aurionpro.app.dto.UserResponse;
import com.aurionpro.app.dto.UserUpdateRequest;
import com.aurionpro.app.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
//
//    @PostMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
//        return new ResponseEntity<>(adminUserService.createUser(request), HttpStatus.CREATED);
//    }
//
//    @GetMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject Pageable pageable) {
//        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
//        return ResponseEntity.ok(adminUserService.getUserById(userId));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long userId, @Valid @RequestBody UserUpdateRequest request) {
//        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
//    }
//
//    // --- NEW RECYCLE BIN ENDPOINTS ---
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Void> softDeleteUser(@PathVariable("id") Long userId) {
//        adminUserService.softDeleteUser(userId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/inactive")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Page<UserResponse>> listInactiveUsers(@ParameterObject Pageable pageable) {
//        return ResponseEntity.ok(adminUserService.listInactiveUsers(pageable));
//    }
//
//    @PostMapping("/{id}/restore")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<UserResponse> restoreUser(@PathVariable("id") Long userId) {
//        return ResponseEntity.ok(adminUserService.restoreUser(userId));
//    }
//
//    @DeleteMapping("/{id}/permanent")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Void> hardDeleteUser(@PathVariable("id") Long userId) {
//        adminUserService.hardDeleteUser(userId);
//        return ResponseEntity.noContent().build();
//    }
}