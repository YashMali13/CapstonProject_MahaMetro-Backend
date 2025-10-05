package com.aurionpro.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloudinary")
@Data
public class CloudinaryProperties {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}