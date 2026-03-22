package com.prjudge.dto.request;

import com.prjudge.domain.enums.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoringRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    @NotNull(message = "Weight is required")
    private Double weight;

    private boolean enabled = true;
    private Double thresholdValue;
    private Long repositoryId;
}
