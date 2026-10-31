package in.gov.npncd.caresetu.repository;
import in.gov.npncd.caresetu.domain.entity.FieldTask;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FieldTaskRepository extends JpaRepository<FieldTask, Long> {
    List<FieldTask> findByAssigneeRoleAndStatusOrderByCreatedAtDesc(String role, String status);
    List<FieldTask> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<FieldTask> findByPublicId(String publicId);
}
