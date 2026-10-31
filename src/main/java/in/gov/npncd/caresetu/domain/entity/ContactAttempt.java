package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "contact_attempts")
public class ContactAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(nullable = false, length = 16)
    private String channel;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1;
    @Column(length = 255)
    private String notes;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
