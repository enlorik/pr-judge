package com.prjudge.repository;

import com.prjudge.domain.entity.PullRequestRecord;
import com.prjudge.domain.enums.PrStatus;
import com.prjudge.domain.enums.RiskCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PullRequestRepository extends JpaRepository<PullRequestRecord, Long> {

    Page<PullRequestRecord> findByRepositoryId(Long repositoryId, Pageable pageable);

    Page<PullRequestRecord> findByStatus(PrStatus status, Pageable pageable);

    @Query("SELECT pr FROM PullRequestRecord pr WHERE " +
           "(:repositoryId IS NULL OR pr.repository.id = :repositoryId) AND " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:riskCategory IS NULL OR pr.scoreResult.riskCategory = :riskCategory)")
    Page<PullRequestRecord> findWithFilters(
            @Param("repositoryId") Long repositoryId,
            @Param("status") PrStatus status,
            @Param("riskCategory") RiskCategory riskCategory,
            Pageable pageable);

    boolean existsByPrNumberAndRepositoryId(Integer prNumber, Long repositoryId);
}
