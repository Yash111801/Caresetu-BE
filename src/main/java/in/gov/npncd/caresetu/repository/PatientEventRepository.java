package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.PatientEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientEventRepository extends JpaRepository<PatientEvent, Long> {
    List<PatientEvent> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
