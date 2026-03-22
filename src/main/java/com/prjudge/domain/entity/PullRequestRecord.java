package com.prjudge.domain.entity;

import com.prjudge.domain.enums.PrStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pull_request_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequestRecord extends BaseEntity {

    @Column(name = "pr_number", nullable = false)
    private Integer prNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "author_login", length = 100)
    private String authorLogin;

    @Column(name = "source_branch", length = 255)
    private String sourceBranch;

    @Column(name = "target_branch", length = 255)
    private String targetBranch;

    @Column(name = "total_additions")
    private Integer totalAdditions;

    @Column(name = "total_deletions")
    private Integer totalDeletions;

    @Column(name = "changed_files_count")
    private Integer changedFilesCount;

    @Column(columnDefinition = "TEXT")
    private String labels;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @OneToOne(mappedBy = "pullRequestRecord", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ScoreResult scoreResult;
}
