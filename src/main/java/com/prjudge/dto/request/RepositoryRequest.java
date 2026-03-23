package com.prjudge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepositoryRequest {

    @NotBlank(message = "Repository name is required")
    private String name;

    @NotBlank(message = "Owner is required")
    private String owner;

    private String githubUrl;
    private String description;
    private boolean active = true;
}
