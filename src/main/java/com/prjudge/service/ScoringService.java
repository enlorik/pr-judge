package com.prjudge.service;

import com.prjudge.domain.entity.Repository;
import com.prjudge.domain.entity.ScoringRule;
import com.prjudge.dto.request.ScoringRuleRequest;
import com.prjudge.dto.response.ScoreResultResponse;
import com.prjudge.exception.ResourceNotFoundException;
import com.prjudge.repository.ScoringRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final ScoringRuleRepository scoringRuleRepository;
    private final RepositoryService repositoryService;

    @Transactional(readOnly = true)
    public List<ScoringRule> findAll() {
        return scoringRuleRepository.findAll();
    }

    @Transactional
    public ScoringRule create(ScoringRuleRequest request) {
        Repository repo = null;
        if (request.getRepositoryId() != null) {
            repo = repositoryService.getOrThrow(request.getRepositoryId());
        }
        ScoringRule rule = ScoringRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ruleType(request.getRuleType())
                .weight(request.getWeight())
                .enabled(request.isEnabled())
                .thresholdValue(request.getThresholdValue())
                .repository(repo)
                .build();
        return scoringRuleRepository.save(rule);
    }

    @Transactional
    public ScoringRule update(Long id, ScoringRuleRequest request) {
        ScoringRule rule = scoringRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScoringRule", id));
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setWeight(request.getWeight());
        rule.setEnabled(request.isEnabled());
        rule.setThresholdValue(request.getThresholdValue());
        if (request.getRepositoryId() != null) {
            rule.setRepository(repositoryService.getOrThrow(request.getRepositoryId()));
        } else {
            rule.setRepository(null);
        }
        return scoringRuleRepository.save(rule);
    }

    @Transactional
    public void delete(Long id) {
        if (!scoringRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("ScoringRule", id);
        }
        scoringRuleRepository.deleteById(id);
    }
}
