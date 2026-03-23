package com.prjudge.dto.response;

import com.prjudge.domain.enums.MergeRecommendation;
import com.prjudge.domain.enums.RiskCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResultResponse {
    private Long id;
    private Double riskScore;
    private Double reviewReadinessScore;
    private RiskCategory riskCategory;
    private MergeRecommendation mergeRecommendation;
    private boolean overridden;
    private String overrideNote;
    private List<ScoreReasonResponse> reasons;
    private Instant createdAt;
    private Instant updatedAt;
}
