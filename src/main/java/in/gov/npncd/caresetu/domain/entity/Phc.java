package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "phcs")
public class Phc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 16)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 100)
    private String taluka;
    @Column(nullable = false, length = 100)
    private String district = "Gandhinagar";
    @Column(name = "state_code", nullable = false, length = 10)
    private String stateCode = "GJ";
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Column(name = "backup_team", nullable = false)
    private boolean backupTeam;
    @Column(nullable = false)
    private boolean active = true;
}
