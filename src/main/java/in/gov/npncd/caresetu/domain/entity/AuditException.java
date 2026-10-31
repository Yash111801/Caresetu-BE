package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_exceptions")
public class AuditException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();

    @Column(name = "area_code", nullable = false, length = 40)
    private String areaCode;

    @Column(name = "area_name", nullable = false, length = 120)
    private String areaName;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "issue_code", nullable = false, length = 40)
    private String issueCode;

    @Column(name = "issue_text", nullable = false, length = 500)
    private String issueText;

    @Column(nullable = false, length = 20)
    private String status = "Open";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "month_key", nullable = false)
    private int monthKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
