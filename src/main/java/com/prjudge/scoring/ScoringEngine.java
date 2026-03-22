package com.prjudge.scoring;

import com.prjudge.domain.entity.ChangedFile;
import com.prjudge.domain.entity.PullRequestRecord;
import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.domain.entity.ScoreResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringEngine {

    private final List<RuleEvaluator> rules;

    /**
     * Scores a pull request based on its metadata and changed files.
     *
     * @return a pair of ScoreResult and the list of ScoreReasons generated
     */
    public ScoringOutput score(PullRequestRecord pr, List<ChangedFile> changedFiles) {
        log.info("Scoring PR #{} in repository {}", pr.getPrNumber(), pr.getRepository().getName());

        ScoringContext ctx = new ScoringContext(pr, changedFiles);

        // Small PR bonus
        if (ctx.totalChanges() < 100) {
            ctx.addReadiness(10);
        }

        List<ScoreReason> reasons = new ArrayList<>();
        for (RuleEvaluator rule : rules) {
            try {
                ScoreReason reason = rule.evaluate(ctx);
                if (reason != null) {
                    reasons.add(reason);
                }
            } catch (Exception e) {
                log.warn("Rule {} threw an exception, skipping", rule.getRuleCode(), e);
            }
        }

        ScoreResult result = ctx.buildResult();
        log.info("PR #{} scored: risk={}, readiness={}, category={}",
                pr.getPrNumber(), result.getRiskScore(), result.getReviewReadinessScore(), result.getRiskCategory());

        return new ScoringOutput(result, reasons);
    }

    public record ScoringOutput(ScoreResult scoreResult, List<ScoreReason> reasons) {}
}
