package in.gov.npncd.caresetu.repository;
import in.gov.npncd.caresetu.domain.entity.PatientMiss;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientMissRepository extends JpaRepository<PatientMiss, Long> {
    List<PatientMiss> findByPatientIdOrderByMissedAtDesc(Long patientId);
    Optional<PatientMiss> findFirstByPatientIdAndResolvedAtIsNullOrderByMissedAtDesc(Long patientId);
    List<PatientMiss> findByResolvedAtIsNull();
}
