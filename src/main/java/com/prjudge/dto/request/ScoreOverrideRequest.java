package com.prjudge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScoreOverrideRequest {

    @NotBlank(message = "Override note is required")
    private String overrideNote;
}
