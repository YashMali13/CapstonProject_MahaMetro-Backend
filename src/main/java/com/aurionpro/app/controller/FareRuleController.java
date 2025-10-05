package com.aurionpro.app.controller;

import com.aurionpro.app.dto.FareRuleRequest;
import com.aurionpro.app.dto.FareRuleResponse;
import com.aurionpro.app.service.FareRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/fare-rules")
@RequiredArgsConstructor
public class FareRuleController {

    private final FareRuleService fareRuleService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FareRuleResponse> createFareRule(@Valid @RequestBody FareRuleRequest request) {
        return new ResponseEntity<>(fareRuleService.createFareRule(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<FareRuleResponse>> listActiveFareRules(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(fareRuleService.listActiveFareRules(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FareRuleResponse> getActiveFareRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(fareRuleService.getActiveFareRuleById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FareRuleResponse> updateFareRule(@PathVariable Long id, @Valid @RequestBody FareRuleRequest request) {
        return ResponseEntity.ok(fareRuleService.updateFareRule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> softDeleteFareRule(@PathVariable Long id) {
        fareRuleService.softDeleteFareRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<FareRuleResponse>> listInactiveFareRules() {
        return ResponseEntity.ok(fareRuleService.listInactiveFareRules());
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FareRuleResponse> restoreFareRule(@PathVariable Long id) {
        return ResponseEntity.ok(fareRuleService.restoreFareRule(id));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> hardDeleteFareRule(@PathVariable Long id) {
        fareRuleService.hardDeleteFareRule(id);
        return ResponseEntity.noContent().build();
    }
}