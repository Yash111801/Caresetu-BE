package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Facility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByCodeAndActiveTrue(String code);
    List<Facility> findByActiveTrueOrderByNameAsc();
}
