package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "facilities")
public class Facility {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 16)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "short_name", nullable = false, length = 80)
    private String shortName;
    @Column(name = "facility_type", nullable = false, length = 80)
    private String facilityType;
    @Column(name = "address_hint", length = 255)
    private String addressHint;
    @Column(length = 120)
    private String hours;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Column(name = "daily_pool_cap", nullable = false)
    private int dailyPoolCap = 10;
    @Column(name = "report_days", nullable = false)
    private int reportDays = 3;
    @Column(name = "services_csv", nullable = false, length = 120)
    private String servicesCsv;
    @Column(nullable = false)
    private boolean active = true;
}
