package com.prjudge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryResponse {
    private Long id;
    private String name;
    private String owner;
    private String githubUrl;
    private String description;
    private boolean active;
    private String createdByUsername;
    private Instant createdAt;
    private Instant updatedAt;
}
