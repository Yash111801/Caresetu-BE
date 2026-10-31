package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPublicIdAndActiveTrue(String publicId);
    Optional<Patient> findByUhid(String uhid);
    List<Patient> findByActiveTrueOrderByUpdatedAtDesc();
    List<Patient> findByPhcCodeAndActiveTrueOrderByUpdatedAtDesc(String phcCode);
    List<Patient> findByAssignedFacCodeAndActiveTrueOrderByUpdatedAtDesc(String facCode);
    List<Patient> findByDistrictAndActiveTrueOrderByUpdatedAtDesc(String district);
    List<Patient> findByStateCodeAndActiveTrueOrderByUpdatedAtDesc(String stateCode);
    long countByStageAndActiveTrue(PatientStage stage);
    long countByPhcCodeAndStageAndActiveTrue(String phcCode, PatientStage stage);
    long countByAssignedFacCodeAndStageAndActiveTrue(String fac, PatientStage stage);

    @Query("""
        select p from Patient p
        where p.active = true
          and (:stage is null or p.stage = :stage)
          and (:phc is null or p.phcCode = :phc)
          and (:fac is null or p.assignedFacCode = :fac)
          and (:q is null or lower(p.fullName) like lower(concat('%', :q, '%'))
               or lower(p.uhid) like lower(concat('%', :q, '%'))
               or p.mobile like concat('%', :q, '%'))
        order by p.updatedAt desc
        """)
    List<Patient> search(@Param("stage") PatientStage stage,
                         @Param("phc") String phc,
                         @Param("fac") String fac,
                         @Param("q") String q);
}
