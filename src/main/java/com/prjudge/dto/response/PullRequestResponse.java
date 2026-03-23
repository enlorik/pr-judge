package com.prjudge.dto.response;

import com.prjudge.domain.enums.PrStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestResponse {
    private Long id;
    private Integer prNumber;
    private String title;
    private String description;
    private String authorLogin;
    private String sourceBranch;
    private String targetBranch;
    private Integer totalAdditions;
    private Integer totalDeletions;
    private Integer changedFilesCount;
    private String labels;
    private PrStatus status;
    private Long repositoryId;
    private String repositoryName;
    private ScoreResultResponse scoreResult;
    private Instant createdAt;
    private Instant updatedAt;
}
