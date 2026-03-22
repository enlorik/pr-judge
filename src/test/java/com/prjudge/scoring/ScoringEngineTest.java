package com.prjudge.scoring;

import com.prjudge.domain.entity.ChangedFile;
import com.prjudge.domain.entity.PullRequestRecord;
import com.prjudge.domain.entity.Repository;
import com.prjudge.domain.entity.ScoreResult;
import com.prjudge.domain.enums.MergeRecommendation;
import com.prjudge.domain.enums.PrStatus;
import com.prjudge.domain.enums.RiskCategory;
import com.prjudge.scoring.rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringEngineTest {

    private ScoringEngine scoringEngine;
    private Repository repo;

    @BeforeEach
    void setUp() {
        List<RuleEvaluator> rules = List.of(
                new LargeDiffRule(),
                new TooManyFilesRule(),
                new SecuritySensitivePathRule(),
                new DatabaseMigrationRule(),
                new MissingTestsRule(),
                new EmptyDescriptionRule(),
                new PoorTitleQualityRule(),
                new MixedConcernsRule()
        );
        scoringEngine = new ScoringEngine(rules);
        repo = new Repository();
        repo.setName("test-repo");
    }

    private PullRequestRecord buildPr(String title, String description, int additions, int deletions) {
        PullRequestRecord pr = new PullRequestRecord();
        pr.setPrNumber(1);
        pr.setTitle(title);
        pr.setDescription(description);
        pr.setTotalAdditions(additions);
        pr.setTotalDeletions(deletions);
        pr.setStatus(PrStatus.OPEN);
        pr.setRepository(repo);
        return pr;
    }

    private ChangedFile file(String path) {
        ChangedFile f = new ChangedFile();
        f.setFilePath(path);
        f.setAdditions(10);
        f.setDeletions(5);
        return f;
    }

    @Test
    void smallCleanPr_shouldBeSafe() {
        PullRequestRecord pr = buildPr(
                "Add user profile page",
                "This PR adds a user profile page with avatar and bio fields.",
                50, 10
        );
        List<ChangedFile> files = List.of(
                file("src/main/UserController.java"),
                file("src/test/UserControllerTest.java")
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        ScoreResult result = output.scoreResult();

        assertThat(result.getRiskCategory()).isEqualTo(RiskCategory.SAFE);
        assertThat(result.getMergeRecommendation()).isEqualTo(MergeRecommendation.MERGE);
        assertThat(result.getRiskScore()).isLessThanOrEqualTo(30);
        assertThat(result.getReviewReadinessScore()).isGreaterThan(80);
    }

    @Test
    void largeDiff_shouldIncreaseRisk() {
        PullRequestRecord pr = buildPr(
                "Refactor entire module",
                "Major refactoring of the module to improve performance.",
                800, 200
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, List.of(file("src/Foo.java")));
        ScoreResult result = output.scoreResult();

        assertThat(result.getRiskScore()).isGreaterThanOrEqualTo(20);
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("LARGE_DIFF"));
    }

    @Test
    void veryLargeDiff_shouldAddMoreRisk() {
        PullRequestRecord pr = buildPr(
                "Massive rewrite",
                "Rewrote everything from scratch.",
                1200, 400
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, List.of(file("src/Foo.java")));
        ScoreResult result = output.scoreResult();

        assertThat(result.getRiskScore()).isGreaterThanOrEqualTo(30);
    }

    @Test
    void securitySensitivePath_shouldAddHighRisk() {
        PullRequestRecord pr = buildPr(
                "Update auth module",
                "Updated authentication logic for better security.",
                50, 10
        );
        List<ChangedFile> files = List.of(
                file("src/auth/AuthService.java"),
                file("src/test/AuthServiceTest.java")
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("SECURITY_SENSITIVE_PATH"));
        assertThat(output.scoreResult().getRiskScore()).isGreaterThanOrEqualTo(25);
    }

    @Test
    void missingTests_shouldAddRisk() {
        PullRequestRecord pr = buildPr(
                "Add feature X",
                "Added a new feature X to the system for better performance.",
                100, 20
        );
        List<ChangedFile> files = List.of(
                file("src/main/FeatureX.java"),
                file("src/main/FeatureXHelper.java")
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("MISSING_TESTS"));
    }

    @Test
    void emptyDescription_shouldAddRisk() {
        PullRequestRecord pr = buildPr("Fix bug", "fix", 20, 5);

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, List.of(file("src/Foo.java")));
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("EMPTY_DESCRIPTION"));
    }

    @Test
    void poorTitle_shouldAddRisk() {
        PullRequestRecord pr = buildPr(
                "hotfix critical bug",
                "Emergency fix for critical production issue affecting all users.",
                30, 10
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, List.of(file("src/Foo.java")));
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("POOR_TITLE_QUALITY"));
    }

    @Test
    void databaseMigration_shouldFlagRisk() {
        PullRequestRecord pr = buildPr(
                "Add user table",
                "Adds a new user table to the database schema.",
                50, 0
        );
        List<ChangedFile> files = List.of(
                file("db/migration/V9__add_users.sql"),
                file("src/test/MigrationTest.java")
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("DATABASE_MIGRATION"));
    }

    @Test
    void highRiskPr_shouldReturnDoNotMerge() {
        PullRequestRecord pr = buildPr("wip hack", "", 1500, 500);
        List<ChangedFile> files = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            files.add(file("module" + i + "/src/File" + i + ".java"));
        }
        files.add(file("src/auth/SecurityConfig.java"));
        files.add(file("db/migration/V99.sql"));

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        assertThat(output.scoreResult().getMergeRecommendation()).isEqualTo(MergeRecommendation.DO_NOT_MERGE);
        assertThat(output.scoreResult().getRiskCategory()).isEqualTo(RiskCategory.HIGH_RISK);
    }

    @Test
    void mixedConcerns_shouldFlagRisk() {
        PullRequestRecord pr = buildPr(
                "Big feature bundle",
                "This PR adds multiple features across different modules of the app.",
                200, 50
        );
        List<ChangedFile> files = List.of(
                file("frontend/src/App.js"),
                file("backend/src/Service.java"),
                file("infra/terraform/main.tf"),
                file("docs/README.md"),
                file("scripts/deploy.sh")
        );

        ScoringEngine.ScoringOutput output = scoringEngine.score(pr, files);
        assertThat(output.reasons()).anyMatch(r -> r.getReasonCode().equals("MIXED_CONCERNS"));
    }
}
