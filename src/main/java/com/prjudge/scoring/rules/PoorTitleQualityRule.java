package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class PoorTitleQualityRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "POOR_TITLE_QUALITY";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (ctx.hasPoorTitleQuality()) {
            ctx.addRisk(10);
            ctx.subtractReadiness(5);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("PR title contains low-quality keywords (hotfix/urgent/temp/fix/wip/hack)")
                    .impactScore(10.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
