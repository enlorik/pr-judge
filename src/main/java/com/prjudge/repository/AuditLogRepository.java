package com.prjudge.repository;

import com.prjudge.domain.entity.AuditLog;
import com.prjudge.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);
}
