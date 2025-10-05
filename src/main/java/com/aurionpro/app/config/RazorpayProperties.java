package com.aurionpro.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "razorpay.key")
@Data
public class RazorpayProperties {
    
    private String id;

  
    private String secret;
}
