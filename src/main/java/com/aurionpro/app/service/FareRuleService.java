package com.aurionpro.app.service;

import com.aurionpro.app.dto.FareRuleRequest;
import com.aurionpro.app.dto.FareRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FareRuleService {

    FareRuleResponse createFareRule(FareRuleRequest request);
    FareRuleResponse getActiveFareRuleById(Long id);
    Page<FareRuleResponse> listActiveFareRules(Pageable pageable);
    FareRuleResponse updateFareRule(Long id, FareRuleRequest request);
    void softDeleteFareRule(Long id);
    List<FareRuleResponse> listInactiveFareRules();
    FareRuleResponse restoreFareRule(Long id);
    void hardDeleteFareRule(Long id);
    
   
}