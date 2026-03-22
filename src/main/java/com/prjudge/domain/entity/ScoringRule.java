package com.prjudge.domain.entity;

import com.prjudge.domain.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scoring_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRule extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;
}
