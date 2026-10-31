package in.gov.npncd.caresetu.repository;
import in.gov.npncd.caresetu.domain.entity.ContactAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ContactAttemptRepository extends JpaRepository<ContactAttempt, Long> {
    List<ContactAttempt> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
