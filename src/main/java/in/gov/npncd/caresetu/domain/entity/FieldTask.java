package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "field_tasks")
public class FieldTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(name = "assignee_role", nullable = false, length = 32)
    private String assigneeRole;
    @Column(nullable = false, length = 32)
    private String kind;
    @Column(name = "task_text", nullable = false, length = 500)
    private String taskText;
    @Column(nullable = false, length = 20)
    private String status = "OPEN";
    @Column(length = 500)
    private String feedback;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
