package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "DATABASE_MIGRATION";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (ctx.hasDatabaseMigrationFiles()) {
            ctx.addRisk(20);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("PR includes database migration files")
                    .impactScore(20.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
