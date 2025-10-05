package com.aurionpro.app.service;

import com.aurionpro.app.common.exception.ResourceNotFoundException;
import com.aurionpro.app.dto.FareRuleRequest;
import com.aurionpro.app.dto.FareRuleResponse;
import com.aurionpro.app.entity.FareRule;
import com.aurionpro.app.mapper.FareRuleMapper;
import com.aurionpro.app.repository.FareRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepository;
    private final FareRuleMapper fareRuleMapper;

    @Override
    public FareRuleResponse createFareRule(FareRuleRequest request) {
        log.info("Creating new fare rule for ticket type: {}", request.getTicketType());
        FareRule fareRule = fareRuleMapper.toEntity(request);
        FareRule savedFareRule = fareRuleRepository.save(fareRule);
        return fareRuleMapper.toResponse(savedFareRule);
    }

    @Override
    @Transactional(readOnly = true)
    public FareRuleResponse getActiveFareRuleById(Long id) {
        return fareRuleRepository.findByIdAndDeletedFalse(id)
                .map(fareRuleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active fare rule not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FareRuleResponse> listActiveFareRules(Pageable pageable) {
        return fareRuleRepository.findAllByDeletedFalse(pageable)
                .map(fareRuleMapper::toResponse);
    }

    @Override
    public FareRuleResponse updateFareRule(Long id, FareRuleRequest request) {
        log.info("Updating fare rule with id: {}", id);
        FareRule existingRule = fareRuleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active fare rule not found with id: " + id));
        
        fareRuleMapper.updateFromDto(request, existingRule);
        FareRule updatedRule = fareRuleRepository.save(existingRule);
        return fareRuleMapper.toResponse(updatedRule);
    }

    @Override
    public void softDeleteFareRule(Long id) {
        log.info("Soft deleting fare rule with id: {}", id);
        if (!fareRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fare rule not found with id: " + id);
        }
        fareRuleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareRuleResponse> listInactiveFareRules() {
        return fareRuleRepository.findAllByDeletedTrue().stream()
                .map(fareRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FareRuleResponse restoreFareRule(Long id) {
        log.info("Restoring fare rule with id: {}", id);
        FareRule inactiveRule = fareRuleRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inactive fare rule not found with id: " + id));
        
        inactiveRule.setDeleted(false);
        FareRule restoredRule = fareRuleRepository.save(inactiveRule);
        return fareRuleMapper.toResponse(restoredRule);
    }

    @Override
    public void hardDeleteFareRule(Long id) {
        log.warn("PERMANENTLY deleting fare rule with id: {}", id);
        if (!fareRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fare rule not found with id: " + id);
        }
        fareRuleRepository.hardDeleteById(id);
    }
}