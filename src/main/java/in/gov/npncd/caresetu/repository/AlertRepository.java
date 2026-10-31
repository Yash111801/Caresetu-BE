package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Alert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByPublicId(String publicId);

    @Query("""
        select a from Alert a
        where (:role is null or a.audienceRole is null or a.audienceRole = :role)
          and (:phc is null or a.audiencePhc is null or a.audiencePhc = :phc)
          and (:fac is null or a.audienceFac is null or a.audienceFac = :fac)
        order by a.createdAt desc
        """)
    List<Alert> findForAudience(@Param("role") String role, @Param("phc") String phc, @Param("fac") String fac);
}
