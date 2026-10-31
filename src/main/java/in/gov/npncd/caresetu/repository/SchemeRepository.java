package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Scheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    List<Scheme> findByActiveTrueOrderByNameAsc();
}
