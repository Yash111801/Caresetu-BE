package in.gov.npncd.caresetu.domain.entity;

import in.gov.npncd.caresetu.domain.enums.CancerType;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.domain.enums.PresentationType;
import in.gov.npncd.caresetu.domain.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true, length = 32)
    private String uhid;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 16)
    private String sex;

    @Column(length = 15)
    private String mobile;

    @Column(name = "abha_id", length = 32)
    private String abhaId;

    @Column(name = "sms_lang", nullable = false, length = 8)
    private String smsLang = "gu";

    @Column(name = "income_band", length = 40)
    private String incomeBand;

    @Column(name = "ration_card", length = 40)
    private String rationCard;

    @Column(name = "has_pmjay_card")
    private Boolean hasPmjayCard;

    @Column(name = "is_esic", nullable = false)
    private boolean esic;

    @Column(name = "is_govt_employee", nullable = false)
    private boolean govtEmployee;

    @Column(name = "has_private_insurance", nullable = false)
    private boolean privateInsurance;

    @Column(name = "gps_consent", nullable = false)
    private boolean gpsConsent;

    @Column(name = "data_consent", nullable = false)
    private boolean dataConsent = true;

    @Column(name = "who_class", length = 80)
    private String whoClass;

    @Column(name = "entry_source", nullable = false, length = 20)
    private String entrySource = "FIELD";

    @Column(length = 120)
    private String village;

    @Column(name = "phc_code", nullable = false, length = 16)
    private String phcCode;

    @Column(length = 100)
    private String taluka;

    @Column(nullable = false, length = 100)
    private String district = "Gandhinagar";

    @Column(name = "state_code", nullable = false, length = 10)
    private String stateCode = "GJ";

    @Enumerated(EnumType.STRING)
    @Column(name = "cancer_type", nullable = false, length = 16)
    private CancerType cancerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PresentationType presentation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PatientStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 16)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Column(name = "screened_by_user_id")
    private Long screenedByUserId;

    @Column(name = "screened_by_name", length = 150)
    private String screenedByName;

    @Column(name = "screened_by_role", length = 32)
    private String screenedByRole;

    @Column(name = "screened_at")
    private Instant screenedAt;

    @Column(name = "finding_notes", length = 1000)
    private String findingNotes;

    @Column(name = "assigned_fac_code", length = 16)
    private String assignedFacCode;

    @Column(name = "visit_at")
    private LocalDate visitAt;

    @Column(name = "visit_window", length = 40)
    private String visitWindow;

    @Column(name = "test_fac_code", length = 16)
    private String testFacCode;

    @Column(name = "report_due")
    private LocalDate reportDue;

    @Column(name = "report_at")
    private Instant reportAt;

    @Column(name = "test_result", length = 40)
    private String testResult;

    @Column(name = "onco_visit_at")
    private LocalDate oncoVisitAt;

    @Column(name = "treatment_plan", length = 120)
    private String treatmentPlan;

    @Column(name = "treatment_started", nullable = false)
    private boolean treatmentStarted;

    @Column(name = "exit_reason", length = 120)
    private String exitReason;

    @Column(name = "informed_at")
    private Instant informedAt;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Column(name = "consult_at")
    private Instant consultAt;

    @Column(name = "closed_reason", length = 120)
    private String closedReason;

    @Column(name = "mo_acked", nullable = false)
    private boolean moAcked;

    @Column(nullable = false)
    private boolean counselled;

    @Column(name = "monitor_until")
    private LocalDate monitorUntil;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
