package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.AuditLog;
import in.gov.npncd.caresetu.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(Long actorUserId, String action, String resource, String resourceId,
                       String ip, String userAgent, boolean success, String detail) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setResource(resource);
        log.setResourceId(resourceId);
        log.setIpAddress(truncate(ip, 45));
        log.setUserAgent(truncate(userAgent, 255));
        log.setSuccess(success);
        log.setDetail(truncate(detail, 500));
        auditLogRepository.save(log);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
