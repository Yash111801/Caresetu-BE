package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "diagnostic_tests")
public class DiagnosticTest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(name = "test_name", nullable = false, length = 120)
    private String testName;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    @Column(name = "result_code", length = 40)
    private String resultCode;
    @Column(name = "result_notes", length = 500)
    private String resultNotes;
    @Column(name = "reported_at")
    private Instant reportedAt;
    @Column(name = "reported_by")
    private Long reportedBy;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
