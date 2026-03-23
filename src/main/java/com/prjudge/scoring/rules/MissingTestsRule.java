package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class MissingTestsRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "MISSING_TESTS";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (!ctx.getChangedFiles().isEmpty()) {
            if (!ctx.hasTestFiles()) {
                ctx.addRisk(15);
                ctx.subtractReadiness(20);
                return ScoreReason.builder()
                        .reasonCode(getRuleCode())
                        .description("No test files found in changed files")
                        .impactScore(15.0)
                        .positive(false)
                        .build();
            } else {
                ctx.addReadiness(10);
                return ScoreReason.builder()
                        .reasonCode("HAS_TESTS")
                        .description("PR includes test files")
                        .impactScore(10.0)
                        .positive(true)
                        .build();
            }
        }
        return null;
    }
}
