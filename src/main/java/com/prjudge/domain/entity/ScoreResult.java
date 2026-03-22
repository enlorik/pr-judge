package com.prjudge.domain.entity;

import com.prjudge.domain.enums.MergeRecommendation;
import com.prjudge.domain.enums.RiskCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "score_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreResult extends BaseEntity {

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "review_readiness_score", nullable = false)
    private Double reviewReadinessScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false, length = 20)
    private RiskCategory riskCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "merge_recommendation", nullable = false, length = 20)
    private MergeRecommendation mergeRecommendation;

    @Column(nullable = false)
    @Builder.Default
    private boolean overridden = false;

    @Column(name = "override_note", columnDefinition = "TEXT")
    private String overrideNote;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_record_id", nullable = false)
    private PullRequestRecord pullRequestRecord;
}
