package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class LargeDiffRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "LARGE_DIFF";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        int total = ctx.totalChanges();
        if (total > 1000) {
            ctx.addRisk(30);
            ctx.subtractReadiness(10);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("Very large diff (" + total + " lines changed > 1000)")
                    .impactScore(30.0)
                    .positive(false)
                    .build();
        } else if (total > 500) {
            ctx.addRisk(20);
            ctx.subtractReadiness(10);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("Large diff (" + total + " lines changed > 500)")
                    .impactScore(20.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
