package com.prjudge.scoring;

import com.prjudge.domain.entity.ChangedFile;
import com.prjudge.domain.entity.PullRequestRecord;
import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.domain.entity.ScoreResult;
import com.prjudge.domain.enums.MergeRecommendation;
import com.prjudge.domain.enums.RiskCategory;
import lombok.Data;

import java.util.List;

/**
 * Intermediate result holder during scoring computation.
 */
@Data
public class ScoringContext {
    private final PullRequestRecord pullRequest;
    private final List<ChangedFile> changedFiles;
    private double riskScore = 0;
    private double readinessScore = 100;

    public int totalChanges() {
        int adds = pullRequest.getTotalAdditions() != null ? pullRequest.getTotalAdditions() : 0;
        int dels = pullRequest.getTotalDeletions() != null ? pullRequest.getTotalDeletions() : 0;
        return adds + dels;
    }

    public int fileCount() {
        return changedFiles.size();
    }

    public boolean hasTestFiles() {
        return changedFiles.stream()
                .anyMatch(f -> f.getFilePath().toLowerCase().contains("test"));
    }

    public boolean hasSecuritySensitiveFiles() {
        List<String> keywords = List.of("auth", "security", "config", "secret", "token", "credential", "password");
        return changedFiles.stream()
                .anyMatch(f -> keywords.stream()
                        .anyMatch(kw -> f.getFilePath().toLowerCase().contains(kw)));
    }

    public boolean hasDatabaseMigrationFiles() {
        List<String> keywords = List.of("migration", "flyway", "liquibase", ".sql");
        return changedFiles.stream()
                .anyMatch(f -> keywords.stream()
                        .anyMatch(kw -> f.getFilePath().toLowerCase().contains(kw)));
    }

    public boolean hasPoorTitleQuality() {
        if (pullRequest.getTitle() == null) return false;
        List<String> keywords = List.of("hotfix", "urgent", "temp", "fix", "wip", "hack");
        String lower = pullRequest.getTitle().toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }

    public boolean hasEmptyDescription() {
        String desc = pullRequest.getDescription();
        return desc == null || desc.trim().length() < 20;
    }

    public boolean hasMixedConcerns() {
        long distinctTopLevel = changedFiles.stream()
                .map(f -> {
                    String path = f.getFilePath();
                    int slash = path.indexOf('/');
                    return slash > 0 ? path.substring(0, slash) : path;
                })
                .distinct()
                .count();
        return distinctTopLevel >= 4;
    }

    public void addRisk(double points) {
        riskScore = Math.min(100, riskScore + points);
    }

    public void subtractReadiness(double points) {
        readinessScore = Math.max(0, readinessScore - points);
    }

    public void addReadiness(double points) {
        readinessScore = Math.min(100, readinessScore + points);
    }

    public ScoreResult buildResult() {
        RiskCategory riskCategory;
        if (riskScore <= 30) {
            riskCategory = RiskCategory.SAFE;
        } else if (riskScore <= 60) {
            riskCategory = RiskCategory.REVIEW_NEEDED;
        } else {
            riskCategory = RiskCategory.HIGH_RISK;
        }

        MergeRecommendation mergeRecommendation = switch (riskCategory) {
            case HIGH_RISK -> MergeRecommendation.DO_NOT_MERGE;
            case REVIEW_NEEDED -> MergeRecommendation.REVIEW_FIRST;
            case SAFE -> MergeRecommendation.MERGE;
        };

        return ScoreResult.builder()
                .riskScore(riskScore)
                .reviewReadinessScore(readinessScore)
                .riskCategory(riskCategory)
                .mergeRecommendation(mergeRecommendation)
                .overridden(false)
                .pullRequestRecord(pullRequest)
                .build();
    }
}
