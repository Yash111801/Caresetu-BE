package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "patient_events")
public class PatientEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(name = "actor_user_id")
    private Long actorUserId;
    @Column(name = "actor_name", length = 150)
    private String actorName;
    @Column(name = "actor_role", length = 32)
    private String actorRole;
    @Column(name = "meta_json", columnDefinition = "json")
    private String metaJson;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
