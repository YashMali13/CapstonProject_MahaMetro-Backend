package com.aurionpro.app.controller;

import com.aurionpro.app.dto.FareRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.service.FareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fare")
@RequiredArgsConstructor
public class FareController {

    private final FareService fareService;

   
    @PostMapping
    public ResponseEntity<FareResponse> calculateFare(@RequestBody @Valid FareRequest request) {
        return ResponseEntity.ok(fareService.calculateFare(request));

    }
}
