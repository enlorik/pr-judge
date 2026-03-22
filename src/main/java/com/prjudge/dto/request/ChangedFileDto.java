package com.prjudge.dto.request;

import lombok.Data;

@Data
public class ChangedFileDto {
    private String filePath;
    private Integer additions;
    private Integer deletions;
}
