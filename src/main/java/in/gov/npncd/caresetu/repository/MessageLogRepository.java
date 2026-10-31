package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.MessageLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {
    List<MessageLog> findTop100ByOrderByCreatedAtDesc();
    List<MessageLog> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
