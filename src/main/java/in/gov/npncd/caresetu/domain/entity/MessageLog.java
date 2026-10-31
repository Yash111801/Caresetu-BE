package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "messages")
public class MessageLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();
    @Column(nullable = false, length = 16)
    private String channel;
    @Column(nullable = false, length = 16)
    private String direction = "OUT";
    @Column(length = 15)
    private String mobile;
    @Column(length = 200)
    private String subject;
    @Column(nullable = false, length = 1000)
    private String body;
    @Column(nullable = false, length = 8)
    private String language = "en";
    @Column(name = "patient_id")
    private Long patientId;
    @Column(nullable = false, length = 20)
    private String status = "QUEUED";
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
