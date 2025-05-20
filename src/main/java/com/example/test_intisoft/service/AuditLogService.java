package com.example.test_intisoft.service;

import com.example.test_intisoft.model.AuditLog;
import com.example.test_intisoft.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.request = request;
    }

    @Transactional
    public void log(String action, String entityType, Long entityId,
                    String username, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setUsername(username);
        log.setIpAddress(getClientIP());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);

        auditLogRepository.save(log);
    }

    private String getClientIP() {
        String ip = request.getHeader("X-Forwarded-For");
        return ip == null ? request.getRemoteAddr() : ip.split(",")[0].trim();
    }
}