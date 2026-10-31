package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "patient_misses")
public class PatientMiss {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(name = "facility_code", nullable = false, length = 16)
    private String facilityCode;
    @Column(name = "missed_at", nullable = false)
    private LocalDate missedAt;
    @Column(nullable = false)
    private boolean onco;
    @Column(name = "escalate_at", nullable = false)
    private LocalDate escalateAt;
    @Column(name = "call1_answered", nullable = false)
    private boolean call1Answered;
    @Column(name = "call2_answered", nullable = false)
    private boolean call2Answered;
    @Column(name = "call3_answered", nullable = false)
    private boolean call3Answered;
    @Column(name = "field_feedback", length = 500)
    private String fieldFeedback;
    @Column(name = "field_at")
    private Instant fieldAt;
    @Column(name = "action_at")
    private Instant actionAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    @Column(name = "resolve_how", length = 80)
    private String resolveHow;
}
