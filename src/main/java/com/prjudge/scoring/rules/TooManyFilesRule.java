package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class TooManyFilesRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "TOO_MANY_FILES";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        int count = ctx.fileCount();
        if (count > 20) {
            ctx.addRisk(20);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("Too many files changed (" + count + " > 20)")
                    .impactScore(20.0)
                    .positive(false)
                    .build();
        } else if (count > 10) {
            ctx.addRisk(15);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("Many files changed (" + count + " > 10)")
                    .impactScore(15.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
