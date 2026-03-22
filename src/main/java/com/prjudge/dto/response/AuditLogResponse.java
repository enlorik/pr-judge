package com.prjudge.dto.response;

import com.prjudge.domain.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private String performedBy;
    private String details;
    private Instant createdAt;
}
