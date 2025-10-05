package com.aurionpro.app.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.aurionpro.app.common.exception.InvalidOtpException;
import com.aurionpro.app.config.CacheConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final EmailService emailService;
    private final CacheManager cacheManager;
    private final Random random;

    @Override
    public void generateAndSendLoginOtp(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000));
        
        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache != null) {
            otpCache.put(email, otp);
            log.info("Stored login OTP for email [{}].", email);
            sendOtpEmail(email, otp); 
        } else {
            log.error("Could not find cache named '{}'. OTP will not be stored.", CacheConfig.OTP_CACHE);
        }
    }

    @Override
    public void sendOtpEmail(String email, String otp) {
        try {
            String subject = "Your Metro System One-Time Password (OTP)";
            String htmlContent = readHtmlTemplate("otp-email.html").replace("${otp}", otp);
            emailService.sendEmail(email, subject, htmlContent);
        } catch (IOException e) {
            log.error("Could not read email template. OTP email not sent to [{}]. Error: {}", email, e.getMessage());
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        Cache otpCache = cacheManager.getCache(CacheConfig.OTP_CACHE);
        if (otpCache == null) {
            log.error("OTP cache not available. Cannot verify OTP for email [{}].", email);
            throw new InvalidOtpException("OTP service is currently unavailable. Please try again later.");
        }
        String cachedOtp = otpCache.get(email, String.class);
        log.info("Verifying login OTP for email [{}].", email);

        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            log.warn("Invalid login OTP attempt for email [{}].", email);
            throw new InvalidOtpException("Invalid or expired OTP. Please try again.");
        }

        otpCache.evict(email);
        log.info("Successfully verified and evicted login OTP for email [{}].", email);
    }
    
    private String readHtmlTemplate(String templateName) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + templateName);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}