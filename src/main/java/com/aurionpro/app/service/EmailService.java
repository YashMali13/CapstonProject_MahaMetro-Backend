package com.aurionpro.app.service;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);
}