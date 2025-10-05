package com.aurionpro.app.controller;

import com.aurionpro.app.dto.CloudinarySignatureResponse;
import com.aurionpro.app.dto.ProfileResponse;
import com.aurionpro.app.dto.UpdateImageUrlRequest;
import com.aurionpro.app.dto.UpdateProfileRequest;
import com.aurionpro.app.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @PostMapping("/upload-signature")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CloudinarySignatureResponse> getUploadSignature() {
        return ResponseEntity.ok(profileService.generateCloudinarySignature());
    }

    @PutMapping("/image-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateImageUrl(@Valid @RequestBody UpdateImageUrlRequest request, Authentication authentication) {
        profileService.updateProfileImageUrl(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyProfileImage(Authentication authentication) {
        profileService.deleteProfileImage(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}