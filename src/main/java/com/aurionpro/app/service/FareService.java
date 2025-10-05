package com.aurionpro.app.service;

import com.aurionpro.app.dto.FareRequest;
import com.aurionpro.app.dto.FareResponse;

public interface FareService {
    
    FareResponse calculateFare(FareRequest request);
}
