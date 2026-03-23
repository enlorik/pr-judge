package com.prjudge.dto.request;

import com.prjudge.domain.enums.PrStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PullRequestIngestRequest {

    @NotNull(message = "PR number is required")
    private Integer prNumber;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String authorLogin;
    private String sourceBranch;
    private String targetBranch;
    private Integer totalAdditions;
    private Integer totalDeletions;
    private Integer changedFilesCount;
    private String labels;
    private PrStatus status = PrStatus.OPEN;

    @NotNull(message = "Repository ID is required")
    private Long repositoryId;

    private List<ChangedFileDto> changedFiles;
}
