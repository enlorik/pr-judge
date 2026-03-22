package com.prjudge.scoring;

import com.prjudge.domain.entity.ScoreReason;

/**
 * Contract for individual scoring rules.
 */
public interface RuleEvaluator {

    /**
     * Returns the unique rule code identifier.
     */
    String getRuleCode();

    /**
     * Evaluates the rule against the context and updates scores.
     * Returns a ScoreReason if the rule fired, null otherwise.
     */
    ScoreReason evaluate(ScoringContext context);
}
