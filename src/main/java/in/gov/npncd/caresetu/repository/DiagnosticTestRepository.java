package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.DiagnosticTest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {
    List<DiagnosticTest> findByPatientIdOrderByIdAsc(Long patientId);
}
