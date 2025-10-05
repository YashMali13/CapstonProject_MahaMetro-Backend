package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.UserCreateRequest;
import com.aurionpro.app.dto.UserResponse;
import com.aurionpro.app.dto.UserUpdateRequest;
import com.aurionpro.app.entity.Role;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Admin creating new user with email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("User with this email already exists.");
        }

        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Successfully created user with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByDeletedFalse(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + userId));
    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("Admin updating user with id: {}", userId);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }


    @Override
    public void softDeleteUser(Long userId) {
        log.info("Attempting to SOFT DELETE user with id: {}", userId);
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + userId));
        
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
        log.info("Successfully soft-deleted user with id: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listInactiveUsers(Pageable pageable) {
        return userRepository.findAllByDeletedTrue(pageable).map(userMapper::toResponse);
    }

    @Override
    public UserResponse restoreUser(Long userId) {
        log.info("Restoring user with id: {}", userId);
        User inactiveUser = userRepository.findByIdAndDeletedTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Inactive user not found with id: " + userId));
        
        inactiveUser.setDeleted(false);
        inactiveUser.setDeletedAt(null);
        User restoredUser = userRepository.save(inactiveUser);
        return userMapper.toResponse(restoredUser);
    }

    @Override
    public void hardDeleteUser(Long userId) {
        log.warn("Attempting to HARD DELETE user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.hardDeleteById(userId);
        log.warn("Successfully hard-deleted user with id: {}", userId);
    }
}