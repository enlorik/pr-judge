package com.prjudge.scoring.rules;

import com.prjudge.domain.entity.ScoreReason;
import com.prjudge.scoring.RuleEvaluator;
import com.prjudge.scoring.ScoringContext;
import org.springframework.stereotype.Component;

@Component
public class SecuritySensitivePathRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "SECURITY_SENSITIVE_PATH";
    }

    @Override
    public ScoreReason evaluate(ScoringContext ctx) {
        if (ctx.hasSecuritySensitiveFiles()) {
            ctx.addRisk(25);
            ctx.subtractReadiness(10);
            return ScoreReason.builder()
                    .reasonCode(getRuleCode())
                    .description("PR touches security-sensitive files (auth/security/config/secret/token/credential/password)")
                    .impactScore(25.0)
                    .positive(false)
                    .build();
        }
        return null;
    }
}
