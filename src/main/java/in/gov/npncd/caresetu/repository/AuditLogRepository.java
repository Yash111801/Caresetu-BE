package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
