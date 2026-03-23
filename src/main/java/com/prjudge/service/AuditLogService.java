package com.prjudge.service;

import com.prjudge.domain.entity.AuditLog;
import com.prjudge.domain.enums.AuditAction;
import com.prjudge.dto.response.AuditLogResponse;
import com.prjudge.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(AuditAction action, String entityType, Long entityId, String performedBy, String details) {
        AuditLog entry = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build();
        auditLogRepository.save(entry);
        log.debug("Audit: {} on {} id={} by {}", action, entityType, entityId, performedBy);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
