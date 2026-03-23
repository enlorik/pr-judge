package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class EmptyDescriptionRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "EMPTY_DESCRIPTION";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (ctx.hasEmptyDescription()) {
            ctx.addRisk(10);
            ctx.subtractReadiness(15);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("PR description is missing or too short (< 20 characters)")
                    .impactScore(10.0)
                    .positive(false)
                    .build();
        } else {
            ctx.addReadiness(10);
            return ScoreReason.builder()
                    .reasonCode("GOOD_DESCRIPTION")
                    .description("PR has a meaningful description")
                    .impactScore(10.0)
                    .positive(true)
                    .build();
        }
    }
}
