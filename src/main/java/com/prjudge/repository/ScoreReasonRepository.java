package com.prjudge.repository;

import com.prjudge.domain.entity.ScoreReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreReasonRepository extends JpaRepository<ScoreReason, Long> {
    List<ScoreReason> findByScoreResultId(Long scoreResultId);
    void deleteByScoreResultId(Long scoreResultId);
}
