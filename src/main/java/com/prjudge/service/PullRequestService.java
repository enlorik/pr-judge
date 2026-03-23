package com.prjudge.service;

import com.prjudge.domain.entity.*;
import com.prjudge.domain.enums.AuditAction;
import com.prjudge.domain.enums.PrStatus;
import com.prjudge.domain.enums.RiskCategory;
import com.prjudge.dto.request.ChangedFileDto;
import com.prjudge.dto.request.PullRequestIngestRequest;
import com.prjudge.dto.request.ScoreOverrideRequest;
import com.prjudge.dto.response.PullRequestResponse;
import com.prjudge.dto.response.ScoreResultResponse;
import com.prjudge.exception.ResourceNotFoundException;
import com.prjudge.exception.ValidationException;
import com.prjudge.repository.ChangedFileRepository;
import com.prjudge.repository.PullRequestRepository;
import com.prjudge.repository.ScoreReasonRepository;
import com.prjudge.repository.ScoreResultRepository;
import com.prjudge.scoring.ScoringEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final ChangedFileRepository changedFileRepository;
    private final ScoreResultRepository scoreResultRepository;
    private final ScoreReasonRepository scoreReasonRepository;
    private final RepositoryService repositoryService;
    private final ScoringEngine scoringEngine;
    private final AuditLogService auditLogService;

    @Transactional
    public PullRequestResponse ingest(PullRequestIngestRequest request, String username) {
        Repository repo = repositoryService.getOrThrow(request.getRepositoryId());

        if (pullRequestRepository.existsByPrNumberAndRepositoryId(request.getPrNumber(), repo.getId())) {
            throw new ValidationException("PR #" + request.getPrNumber() + " already exists for this repository");
        }

        PullRequestRecord pr = PullRequestRecord.builder()
                .prNumber(request.getPrNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .authorLogin(request.getAuthorLogin())
                .sourceBranch(request.getSourceBranch())
                .targetBranch(request.getTargetBranch())
                .totalAdditions(request.getTotalAdditions() != null ? request.getTotalAdditions() : 0)
                .totalDeletions(request.getTotalDeletions() != null ? request.getTotalDeletions() : 0)
                .changedFilesCount(request.getChangedFilesCount() != null ? request.getChangedFilesCount() : 0)
                .labels(request.getLabels())
                .status(request.getStatus() != null ? request.getStatus() : PrStatus.OPEN)
                .repository(repo)
                .build();

        PullRequestRecord saved = pullRequestRepository.save(pr);

        List<ChangedFile> changedFiles = new ArrayList<>();
        if (request.getChangedFiles() != null) {
            for (ChangedFileDto dto : request.getChangedFiles()) {
                ChangedFile file = ChangedFile.builder()
                        .filePath(dto.getFilePath())
                        .additions(dto.getAdditions() != null ? dto.getAdditions() : 0)
                        .deletions(dto.getDeletions() != null ? dto.getDeletions() : 0)
                        .pullRequestRecord(saved)
                        .build();
                changedFiles.add(changedFileRepository.save(file));
            }
        }

        auditLogService.log(AuditAction.PR_INGESTED, "PullRequestRecord", saved.getId(), username,
                "PR #" + saved.getPrNumber() + " ingested for repo " + repo.getName());

        // Auto-score after ingest
        ScoringEngine.ScoringOutput output = scoringEngine.score(saved, changedFiles);
        persistScore(saved, output, username);

        PullRequestRecord refreshed = pullRequestRepository.findById(saved.getId()).orElse(saved);
        return toResponse(refreshed);
    }

    @Transactional(readOnly = true)
    public Page<PullRequestResponse> findAll(Long repositoryId, PrStatus status, RiskCategory riskCategory, Pageable pageable) {
        return pullRequestRepository.findWithFilters(repositoryId, status, riskCategory, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PullRequestResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ScoreResultResponse rescore(Long id, String username) {
        PullRequestRecord pr = getOrThrow(id);
        List<ChangedFile> files = changedFileRepository.findByPullRequestRecordId(id);

        // Delete existing score
        scoreResultRepository.findByPullRequestRecordId(id).ifPresent(existing -> {
            scoreReasonRepository.deleteByScoreResultId(existing.getId());
            scoreResultRepository.delete(existing);
        });

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        ScoreResult saved = persistScore(pr, output, username);
        return toScoreResponse(saved);
    }

    @Transactional
    public ScoreResultResponse override(Long id, ScoreOverrideRequest request, String username) {
        ScoreResult score = scoreResultRepository.findByPullRequestRecordId(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScoreResult for PR", id));
        score.setOverridden(true);
        score.setOverrideNote(request.getOverrideNote());
        ScoreResult saved = scoreResultRepository.save(score);

        auditLogService.log(AuditAction.SCORE_OVERRIDDEN, "ScoreResult", saved.getId(), username,
                "Override note: " + request.getOverrideNote());

        return toScoreResponse(saved);
    }

    @Transactional(readOnly = true)
    public ScoreResultResponse getScore(Long id) {
        ScoreResult score = scoreResultRepository.findByPullRequestRecordId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Score for PR", id));
        return toScoreResponse(score);
    }

    private ScoreResult persistScore(PullRequestRecord pr, ScoringEngine.ScoringOutput output, String username) {
        ScoreResult score = output.scoreResult();
        score.setPullRequestRecord(pr);
        ScoreResult savedScore = scoreResultRepository.save(score);

        for (ScoreReason reason : output.reasons()) {
            reason.setScoreResult(savedScore);
            scoreReasonRepository.save(reason);
        }

        auditLogService.log(AuditAction.SCORE_CALCULATED, "PullRequestRecord", pr.getId(), username,
                "Risk: " + savedScore.getRiskScore() + ", Readiness: " + savedScore.getReviewReadinessScore());

        return savedScore;
    }

    public PullRequestRecord getOrThrow(Long id) {
        return pullRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", id));
    }

    private PullRequestResponse toResponse(PullRequestRecord pr) {
        ScoreResultResponse scoreResponse = null;
        if (pr.getScoreResult() != null) {
            scoreResponse = toScoreResponse(pr.getScoreResult());
        }
        return PullRequestResponse.builder()
                .id(pr.getId())
                .prNumber(pr.getPrNumber())
                .title(pr.getTitle())
                .description(pr.getDescription())
                .authorLogin(pr.getAuthorLogin())
                .sourceBranch(pr.getSourceBranch())
                .targetBranch(pr.getTargetBranch())
                .totalAdditions(pr.getTotalAdditions())
                .totalDeletions(pr.getTotalDeletions())
                .changedFilesCount(pr.getChangedFilesCount())
                .labels(pr.getLabels())
                .status(pr.getStatus())
                .repositoryId(pr.getRepository().getId())
                .repositoryName(pr.getRepository().getName())
                .scoreResult(scoreResponse)
                .createdAt(pr.getCreatedAt())
                .updatedAt(pr.getUpdatedAt())
                .build();
    }

    private ScoreResultResponse toScoreResponse(ScoreResult score) {
        List<com.prjudge.dto.response.ScoreReasonResponse> reasons =
                scoreReasonRepository.findByScoreResultId(score.getId()).stream()
                        .map(r -> com.prjudge.dto.response.ScoreReasonResponse.builder()
                                .id(r.getId())
                                .reasonCode(r.getReasonCode())
                                .description(r.getDescription())
                                .impactScore(r.getImpactScore())
                                .positive(r.isPositive())
                                .build())
                        .toList();

        return ScoreResultResponse.builder()
                .id(score.getId())
                .riskScore(score.getRiskScore())
                .reviewReadinessScore(score.getReviewReadinessScore())
                .riskCategory(score.getRiskCategory())
                .mergeRecommendation(score.getMergeRecommendation())
                .overridden(score.isOverridden())
                .overrideNote(score.getOverrideNote())
                .reasons(reasons)
                .createdAt(score.getCreatedAt())
                .updatedAt(score.getUpdatedAt())
                .build();
    }
}
