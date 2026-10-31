package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "alerts")
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 500)
    private String body;
    @Column(nullable = false, length = 16)
    private String severity = "INFO";
    @Column(name = "audience_role", length = 32)
    private String audienceRole;
    @Column(name = "audience_phc", length = 16)
    private String audiencePhc;
    @Column(name = "audience_fac", length = 16)
    private String audienceFac;
    @Column(name = "audience_district", length = 100)
    private String audienceDistrict;
    @Column(name = "patient_id")
    private Long patientId;
    @Column(name = "read_flag", nullable = false)
    private boolean readFlag;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
