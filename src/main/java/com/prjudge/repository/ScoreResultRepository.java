package com.prjudge.repository;

import com.prjudge.domain.entity.ScoreResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreResultRepository extends JpaRepository<ScoreResult, Long> {
    Optional<ScoreResult> findByPullRequestRecordId(Long pullRequestRecordId);
}
