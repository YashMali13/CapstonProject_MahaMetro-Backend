package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.CloudinarySignatureResponse;
import com.aurionpro.app.dto.ProfileResponse;
import com.aurionpro.app.dto.UpdateImageUrlRequest;
import com.aurionpro.app.dto.UpdateProfileRequest;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.mapper.UserMapper;
import com.aurionpro.app.repository.UserRepository;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Cloudinary cloudinary;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(userMapper::toProfileResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
    }

    @Override
    public ProfileResponse updateProfile(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated for email [{}]", userEmail);
        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    public CloudinarySignatureResponse generateCloudinarySignature() {
        long timestamp = System.currentTimeMillis() / 1000L;
        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        paramsToSign.put("folder", "metro_profile_pics");

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);
        
        return new CloudinarySignatureResponse(
                signature,
                timestamp,
                cloudinary.config.apiKey,
                cloudinary.config.cloudName
        );
    }

    @Override
    public void updateProfileImageUrl(String userEmail, UpdateImageUrlRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        user.setProfileImageUrl(request.getImageUrl());
        userRepository.save(user);
        log.info("Updated profile image URL for user [{}]", userEmail);
    }

    @Override
    public void deleteProfileImage(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                String url = user.getProfileImageUrl();
                // Assumes a folder structure like "metro_profile_pics/public_id.jpg"
                String publicIdWithFolder = url.substring(url.indexOf("metro_profile_pics"));
                String publicId = publicIdWithFolder.substring(0, publicIdWithFolder.lastIndexOf("."));
                
                cloudinary.uploader().destroy(publicId, null);
                log.info("Deleted image from Cloudinary for user [{}]", userEmail);

            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary for user [{}]. Will only clear DB link.", userEmail, e);
            }
        }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }
}