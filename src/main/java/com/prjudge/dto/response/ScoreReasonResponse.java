package com.prjudge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreReasonResponse {
    private Long id;
    private String reasonCode;
    private String description;
    private Double impactScore;
    private boolean positive;
}
