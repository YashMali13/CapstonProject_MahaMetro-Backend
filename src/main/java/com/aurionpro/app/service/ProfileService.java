package com.aurionpro.app.service;

import com.aurionpro.app.dto.CloudinarySignatureResponse;
import com.aurionpro.app.dto.ProfileResponse;
import com.aurionpro.app.dto.UpdateImageUrlRequest;
import com.aurionpro.app.dto.UpdateProfileRequest;

public interface ProfileService {
    ProfileResponse getProfile(String userEmail);
    ProfileResponse updateProfile(String userEmail, UpdateProfileRequest request);
    CloudinarySignatureResponse generateCloudinarySignature();
    void updateProfileImageUrl(String userEmail, UpdateImageUrlRequest request);
    void deleteProfileImage(String userEmail);
}