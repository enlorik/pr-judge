package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class MixedConcernsRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "MIXED_CONCERNS";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (ctx.hasMixedConcerns()) {
            ctx.addRisk(10);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("PR spans many different top-level directories indicating mixed concerns")
                    .impactScore(10.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
