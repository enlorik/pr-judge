package com.prjudge.repository;

import com.prjudge.domain.entity.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {

    @Query("SELECT sr FROM ScoringRule sr WHERE sr.enabled = true AND (sr.repository IS NULL OR sr.repository.id = :repositoryId)")
    List<ScoringRule> findActiveRulesForRepository(Long repositoryId);

    List<ScoringRule> findByRepositoryIsNullAndEnabledTrue();
}
