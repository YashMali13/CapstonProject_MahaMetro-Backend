package com.aurionpro.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateImageUrlRequest {
    @NotBlank(message = "Image URL is required.")
    @URL(message = "A valid URL is required.")
    private String imageUrl;
}