package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.AuditException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditExceptionRepository extends JpaRepository<AuditException, Long> {
    List<AuditException> findByMonthKeyOrderByStatusAscAreaNameAsc(int monthKey);
}
