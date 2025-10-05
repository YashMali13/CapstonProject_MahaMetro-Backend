package com.aurionpro.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface WebhookProcessingService {
    void processRazorpayEvent(String eventId, String eventType, String payload, String signature) throws JsonProcessingException;
}