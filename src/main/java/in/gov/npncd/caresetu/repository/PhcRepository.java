package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Phc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhcRepository extends JpaRepository<Phc, Long> {
    Optional<Phc> findByCodeAndActiveTrue(String code);
    List<Phc> findByActiveTrueOrderByNameAsc();
}
