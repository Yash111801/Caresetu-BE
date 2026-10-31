package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Booking;
import in.gov.npncd.caresetu.domain.entity.DiagnosticTest;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import in.gov.npncd.caresetu.domain.enums.CancerType;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.domain.enums.PresentationType;
import in.gov.npncd.caresetu.domain.enums.RiskLevel;
import in.gov.npncd.caresetu.dto.patient.AddHospitalPatientRequest;
import in.gov.npncd.caresetu.dto.patient.ApproveScreeningRequest;
import in.gov.npncd.caresetu.dto.patient.AttendVisitRequest;
import in.gov.npncd.caresetu.dto.patient.CloseCaseRequest;
import in.gov.npncd.caresetu.dto.patient.ConsultRequest;
import in.gov.npncd.caresetu.dto.patient.CreateScreeningRequest;
import in.gov.npncd.caresetu.dto.patient.EligibilityRequest;
import in.gov.npncd.caresetu.dto.patient.HomeDto;
import in.gov.npncd.caresetu.dto.patient.PatientDetailDto;
import in.gov.npncd.caresetu.dto.patient.PatientSummaryDto;
import in.gov.npncd.caresetu.dto.patient.ReferBookRequest;
import in.gov.npncd.caresetu.dto.patient.ReportTestRequest;
import in.gov.npncd.caresetu.exception.ApiException;
import in.gov.npncd.caresetu.repository.BookingRepository;
import in.gov.npncd.caresetu.repository.DiagnosticTestRepository;
import in.gov.npncd.caresetu.repository.PatientEventRepository;
import in.gov.npncd.caresetu.repository.PatientRepository;
import in.gov.npncd.caresetu.repository.PhcRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientEventRepository patientEventRepository;
    private final BookingRepository bookingRepository;
    private final DiagnosticTestRepository diagnosticTestRepository;
    private final PhcRepository phcRepository;
    private final ScopeService scopeService;
    private final PatientMapperService mapper;
    private final PatientEventService eventService;
    private final FacilityService facilityService;
    private final NotificationDispatchService notifications;
    private final AuditService auditService;
    private final MissedVisitService missedVisitService;

    public PatientService(
            PatientRepository patientRepository,
            PatientEventRepository patientEventRepository,
            BookingRepository bookingRepository,
            DiagnosticTestRepository diagnosticTestRepository,
            PhcRepository phcRepository,
            ScopeService scopeService,
            PatientMapperService mapper,
            PatientEventService eventService,
            FacilityService facilityService,
            NotificationDispatchService notifications,
            AuditService auditService,
            MissedVisitService missedVisitService) {
        this.patientRepository = patientRepository;
        this.patientEventRepository = patientEventRepository;
        this.bookingRepository = bookingRepository;
        this.diagnosticTestRepository = diagnosticTestRepository;
        this.phcRepository = phcRepository;
        this.scopeService = scopeService;
        this.mapper = mapper;
        this.eventService = eventService;
        this.facilityService = facilityService;
        this.notifications = notifications;
        this.auditService = auditService;
        this.missedVisitService = missedVisitService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PATIENT_READ')")
    public HomeDto home(UserPrincipal principal) {
        UserAccount user = scopeService.requireUser(principal);
        String role = scopeService.primaryRole(principal);
        List<Patient> scoped = scopeService.patientsInScope(principal);
        List<PatientSummaryDto> worklist = scoped.stream()
                .map(p -> mapper.toSummary(p, principal))
                .filter(s -> s.actions() != null && !s.actions().isEmpty())
                .limit(20)
                .toList();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) scoped.size());
        stats.put("open", scoped.stream().filter(p -> p.getStage() != PatientStage.CLOSED).count());
        stats.put("booked", scoped.stream().filter(p -> p.getStage() == PatientStage.BOOKED).count());
        stats.put("highRisk", scoped.stream().filter(p -> p.getRiskLevel() == RiskLevel.HIGH).count());
        stats.put("missed", scoped.stream().filter(p -> p.getStage() == PatientStage.MISSED).count());
        return new HomeDto(
                "Namaste, " + user.getFullName(),
                role,
                stats,
                worklist,
                List.of(
                        "Green-channel pool: " + FacilityService.POOL_CAP + " places a day per hospital",
                        "OTP / SMS / IVRS are demo-simulated until TRAI DLT gateway is connected"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PATIENT_READ')")
    public List<PatientSummaryDto> list(UserPrincipal principal, String stage, String q) {
        PatientStage stageEnum = null;
        if (stage != null && !stage.isBlank()) {
            stageEnum = PatientStage.valueOf(stage.trim().toUpperCase(Locale.ROOT));
        }
        final PatientStage filterStage = stageEnum;
        return scopeService.patientsInScope(principal).stream()
                .filter(p -> filterStage == null || p.getStage() == filterStage)
                .filter(p -> q == null || q.isBlank()
                        || p.getFullName().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))
                        || p.getUhid().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))
                        || (p.getMobile() != null && p.getMobile().contains(q)))
                .map(p -> mapper.toSummary(p, principal))
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PATIENT_READ')")
    public PatientDetailDto get(UserPrincipal principal, String publicId) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        return mapper.toDetail(
                patient,
                principal,
                patientEventRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()),
                bookingRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()),
                diagnosticTestRepository.findByPatientIdOrderByIdAsc(patient.getId()));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_SCREENING_WRITE')")
    public PatientDetailDto createScreening(UserPrincipal principal, CreateScreeningRequest req) {
        UserAccount user = scopeService.requireUser(principal);
        String role = scopeService.primaryRole(principal);
        if (!List.of("ASHA", "CHO", "MO", "HOSP").contains(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Role cannot create screening");
        }
        phcRepository.findByCodeAndActiveTrue(req.phcCode())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHC", "Unknown PHC"));

        CancerType type = CancerType.valueOf(req.cancerType().toLowerCase(Locale.ROOT));
        PresentationType presentation = PresentationType.valueOf(req.presentation().toLowerCase(Locale.ROOT));

        Patient patient = new Patient();
        patient.setPublicId(UUID.randomUUID().toString());
        patient.setUhid(nextUhid());
        patient.setFullName(req.fullName().trim());
        patient.setAge(req.age());
        patient.setSex(req.sex());
        patient.setMobile(blankToNull(req.mobile()));
        patient.setVillage(req.village().trim());
        patient.setPhcCode(req.phcCode());
        patient.setTaluka(user.getTaluka());
        patient.setDistrict(user.getDistrict() != null ? user.getDistrict() : "Gandhinagar");
        patient.setStateCode(user.getStateCode() != null ? user.getStateCode() : "GJ");
        patient.setCancerType(type);
        patient.setPresentation(presentation);
        patient.setFindingNotes(req.findingNotes());
        patient.setScreenedByUserId(user.getId());
        patient.setScreenedByName(user.getFullName());
        patient.setScreenedByRole(role);
        patient.setScreenedAt(Instant.now());
        patient.setCounselled(Boolean.TRUE.equals(req.counselled()));
        patient.setSmsLang("gu");
        patient.setEntrySource("FIELD");
        patient.setDataConsent(true);
        patient.setWhoClass(whoFor(type));

        if (presentation == PresentationType.asymptomatic) {
            patient.setStage(PatientStage.MONITOR);
            patient.setRiskLevel(RiskLevel.LOW);
            patient.setMonitorUntil(LocalDate.now().plusDays(90));
        } else if ("ASHA".equals(role)) {
            patient.setStage(PatientStage.CHO);
            patient.setRiskLevel(RiskLevel.HIGH);
        } else if ("CHO".equals(role)) {
            patient.setStage(PatientStage.REVIEW);
            patient.setRiskLevel(RiskLevel.HIGH);
        } else {
            patient.setStage(PatientStage.REVIEW);
            patient.setRiskLevel(RiskLevel.HIGH);
        }

        patientRepository.save(patient);
        eventService.add(patient.getId(), "SCREENED",
                "Screened for " + type + " · " + presentation, principal);

        if (patient.getStage() == PatientStage.CHO) {
            notifications.alert("CHO review pending", patient.getFullName() + " awaiting CHO review",
                    "WARN", "CHO", patient.getPhcCode(), null, patient);
        } else if (patient.getStage() == PatientStage.REVIEW) {
            notifications.alert("MO booking pending", patient.getFullName() + " ready for approve & book",
                    "WARN", "MO", patient.getPhcCode(), null, patient);
        }

        auditService.record(principal.getId(), "SCREENING_CREATE", "patient", patient.getPublicId(),
                null, null, true, null);
        return get(principal, patient.getPublicId());
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_SCREENING_WRITE')")
    public PatientDetailDto approveByCho(UserPrincipal principal, String publicId, ApproveScreeningRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.CHO);
        if (!"forward".equalsIgnoreCase(req.outcome()) && !"monitor".equalsIgnoreCase(req.outcome())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OUTCOME", "outcome must be forward or monitor");
        }
        if ("monitor".equalsIgnoreCase(req.outcome())) {
            patient.setStage(PatientStage.MONITOR);
            patient.setRiskLevel(RiskLevel.LOW);
            patient.setMonitorUntil(LocalDate.now().plusDays(90));
            eventService.add(patient.getId(), "CHO_MONITOR", "CHO classified for monitoring · " + nullSafe(req.notes()), principal);
        } else {
            patient.setStage(PatientStage.REVIEW);
            patient.setRiskLevel(RiskLevel.HIGH);
            eventService.add(patient.getId(), "CHO_FORWARD", "CHO forwarded to PHC MO · " + nullSafe(req.notes()), principal);
            notifications.alert("MO booking pending", patient.getFullName() + " forwarded by CHO",
                    "WARN", "MO", patient.getPhcCode(), null, patient);
        }
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REFERRAL_WRITE')")
    public PatientDetailDto referAndBook(UserPrincipal principal, String publicId, ReferBookRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        if (patient.getStage() != PatientStage.REVIEW && patient.getStage() != PatientStage.MISSED) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STAGE", "Patient not ready for booking");
        }
        var facility = facilityService.requireFacility(req.facilityCode());
        if (!facility.getServicesCsv().contains(patient.getCancerType().name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SERVICE_UNAVAILABLE",
                    "Facility does not offer this cancer pathway");
        }
        int place = facilityService.nextPlaceNo(req.facilityCode(), req.bookingDate());

        bookingRepository.findFirstByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), BookingStatus.ACTIVE)
                .ifPresent(b -> {
                    b.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(b);
                });

        Booking booking = new Booking();
        booking.setPublicId(UUID.randomUUID().toString());
        booking.setPatientId(patient.getId());
        booking.setFacilityCode(req.facilityCode());
        booking.setBookingDate(req.bookingDate());
        booking.setWindowLabel(req.windowLabel());
        booking.setPlaceNo(place);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setBookedByUserId(principal.getId());
        booking.setBookedByName(principal.getFullName());
        booking.setBookingKind("HOSPITAL");
        bookingRepository.save(booking);

        patient.setStage(PatientStage.BOOKED);
        patient.setAssignedFacCode(req.facilityCode());
        patient.setVisitAt(req.bookingDate());
        patient.setVisitWindow(req.windowLabel());
        patient.setMoAcked(true);
        patientRepository.save(patient);

        String msg = "Booked at " + facility.getShortName() + " for " + req.bookingDate()
                + ", " + req.windowLabel() + " · place " + place + " of " + facility.getDailyPoolCap();
        eventService.add(patient.getId(), "BOOKED", msg + " · SMS and voice call sent", principal);
        notifications.sms(patient, "CARESETU booking",
                "CARESETU: Visit booked on " + req.bookingDate() + " " + req.windowLabel()
                        + " at " + facility.getName() + ". Place " + place + ".");
        notifications.ivrs(patient, "CARESETU reminder", "Voice reminder queued for hospital visit.");
        notifications.alert("Hospital visit booked", patient.getFullName() + " · " + facility.getShortName(),
                "INFO", "HOSP", null, facility.getCode(), patient);

        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_WRITE')")
    public PatientDetailDto attend(UserPrincipal principal, String publicId, AttendVisitRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.BOOKED);
        bookingRepository.findFirstByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), BookingStatus.ACTIVE)
                .ifPresent(b -> {
                    b.setStatus(BookingStatus.COMPLETED);
                    bookingRepository.save(b);
                });

        if ("cant_serve".equalsIgnoreCase(req.outcome()) || "cantserve".equalsIgnoreCase(req.outcome())) {
            missedVisitService.startMiss(patient, false, principal);
            eventService.add(patient.getId(), "CANT_SERVE",
                    "Hospital could not serve · " + nullSafe(req.notes()), principal);
            return get(principal, publicId);
        }
        if ("missed".equalsIgnoreCase(req.outcome())) {
            missedVisitService.startMiss(patient, false, principal);
            return get(principal, publicId);
        }

        patient.setStage(PatientStage.TESTED);
        patient.setAssessedAt(Instant.now());
        patient.setTestFacCode(patient.getAssignedFacCode());
        patient.setReportDue(LocalDate.now().plusDays(
                facilityService.requireFacility(patient.getAssignedFacCode()).getReportDays()));
        patientRepository.save(patient);

        DiagnosticTest test = new DiagnosticTest();
        test.setPatientId(patient.getId());
        test.setTestName(defaultTestName(patient.getCancerType()));
        test.setStatus("PENDING");
        diagnosticTestRepository.save(test);

        eventService.add(patient.getId(), "ATTENDED",
                "Visit recorded · tests ordered · " + nullSafe(req.notes()), principal);
        notifications.alert("Tests pending", patient.getFullName() + " awaiting diagnostic report",
                "INFO", "PATH", null, patient.getTestFacCode(), patient);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REPORT_WRITE')")
    public PatientDetailDto reportTest(UserPrincipal principal, String publicId, ReportTestRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.TESTED);
        DiagnosticTest test = diagnosticTestRepository.findById(req.testId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TEST_NOT_FOUND", "Test not found"));
        if (!test.getPatientId().equals(patient.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TEST_MISMATCH", "Test does not belong to patient");
        }
        test.setStatus("DONE");
        test.setResultCode(req.resultCode().toUpperCase(Locale.ROOT));
        test.setResultNotes(req.resultNotes());
        test.setReportedAt(Instant.now());
        test.setReportedBy(principal.getId());
        diagnosticTestRepository.save(test);

        patient.setReportAt(Instant.now());
        patient.setTestResult(test.getResultCode());
        if ("POSITIVE".equalsIgnoreCase(test.getResultCode())) {
            patient.setStage(PatientStage.ONCO_REQ);
            eventService.add(patient.getId(), "REPORT_POSITIVE",
                    "Diagnostic positive · oncology booking required", principal);
            notifications.alert("Oncology booking needed", patient.getFullName() + " biopsy/test positive",
                    "CRIT", "MO", patient.getPhcCode(), null, patient);
        } else if ("NEGATIVE".equalsIgnoreCase(test.getResultCode())) {
            patient.setStage(PatientStage.CLOSED);
            patient.setClosedReason("Not cancer on diagnostic review");
            eventService.add(patient.getId(), "REPORT_NEGATIVE", "Diagnostic negative · pathway closed", principal);
        } else {
            eventService.add(patient.getId(), "REPORT_OTHER",
                    "Result " + test.getResultCode() + " · " + nullSafe(req.resultNotes()), principal);
        }
        patientRepository.save(patient);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REFERRAL_WRITE')")
    public PatientDetailDto bookOncology(UserPrincipal principal, String publicId, ReferBookRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.ONCO_REQ);
        if (!"RCC".equals(req.facilityCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FACILITY", "Oncology must be booked at RCC");
        }
        int place = facilityService.nextPlaceNo("RCC", req.bookingDate());
        Booking booking = new Booking();
        booking.setPublicId(UUID.randomUUID().toString());
        booking.setPatientId(patient.getId());
        booking.setFacilityCode("RCC");
        booking.setBookingDate(req.bookingDate());
        booking.setWindowLabel(req.windowLabel());
        booking.setPlaceNo(place);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setBookedByUserId(principal.getId());
        booking.setBookedByName(principal.getFullName());
        booking.setBookingKind("ONCOLOGY");
        bookingRepository.save(booking);

        patient.setStage(PatientStage.SPECIALIST);
        patient.setAssignedFacCode("RCC");
        patient.setOncoVisitAt(req.bookingDate());
        patient.setVisitWindow(req.windowLabel());
        patientRepository.save(patient);

        eventService.add(patient.getId(), "ONCO_BOOKED",
                "First oncology consultation booked at Cancer Centre · place " + place, principal);
        notifications.sms(patient, "CARESETU oncology",
                "CARESETU: Oncology visit on " + req.bookingDate() + " at Regional Cancer Centre.");
        notifications.alert("Oncology visit", patient.getFullName() + " booked at RCC",
                "INFO", "ONCO", null, "RCC", patient);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_WRITE')")
    public PatientDetailDto consult(UserPrincipal principal, String publicId, ConsultRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.SPECIALIST);
        patient.setTreatmentPlan(req.treatmentPlan());
        patient.setConsultAt(Instant.now());
        if ("not_cancer".equalsIgnoreCase(req.outcome())) {
            patient.setStage(PatientStage.CLOSED);
            patient.setClosedReason("Not cancer on review");
        } else {
            patient.setStage(PatientStage.CLOSED);
            patient.setClosedReason(req.outcome() + (req.treatmentPlan() != null ? " · " + req.treatmentPlan() : ""));
        }
        patientRepository.save(patient);
        missedVisitService.resolveMiss(patient, "oncology consult completed", principal);
        eventService.add(patient.getId(), "CONSULT",
                "Oncology consultation · " + req.outcome() + " · " + nullSafe(req.notes()), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PATIENT_WRITE')")
    public PatientDetailDto acknowledge(UserPrincipal principal, String publicId, String notes) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setMoAcked(true);
        patientRepository.save(patient);
        eventService.add(patient.getId(), "MO_ACK", "MO acknowledged hospital registration · " + nullSafe(notes), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REPORT_WRITE')")
    public PatientDetailDto markInconclusive(UserPrincipal principal, String publicId, String notes) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.TESTED);
        patient.setTestResult("INCONCLUSIVE");
        patient.setReportDue(LocalDate.now().plusDays(7));
        patient.setReportAt(Instant.now());
        patientRepository.save(patient);
        DiagnosticTest repeat = new DiagnosticTest();
        repeat.setPatientId(patient.getId());
        repeat.setTestName("Repeat " + defaultTestName(patient.getCancerType()));
        repeat.setStatus("PENDING");
        diagnosticTestRepository.save(repeat);
        eventService.add(patient.getId(), "INCONCLUSIVE",
                "Inconclusive · repeat ordered · " + nullSafe(notes), principal);
        notifications.alert("Repeat test needed", patient.getFullName() + " inconclusive result",
                "WARN", "PATH", null, patient.getTestFacCode(), patient);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_REFERRAL_WRITE','PERM_APPOINTMENT_WRITE')")
    public PatientDetailDto startTreatment(UserPrincipal principal, String publicId, String plan, String notes) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setTreatmentStarted(true);
        if (plan != null && !plan.isBlank()) {
            patient.setTreatmentPlan(plan);
        }
        patientRepository.save(patient);
        eventService.add(patient.getId(), "TX_START",
                "Treatment started · " + nullSafe(plan) + " · " + nullSafe(notes), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_REFERRAL_WRITE','PERM_PATIENT_WRITE')")
    public PatientDetailDto exitPathway(UserPrincipal principal, String publicId, String reason, String notes) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setStage(PatientStage.CLOSED);
        patient.setExitReason(reason);
        patient.setClosedReason(reason);
        patientRepository.save(patient);
        missedVisitService.resolveMiss(patient, "exit: " + reason, principal);
        eventService.add(patient.getId(), "EXIT", reason + " · " + nullSafe(notes), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PATIENT_WRITE')")
    public PatientDetailDto fieldVisit(UserPrincipal principal, String publicId, String feedback) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setInformedAt(Instant.now());
        patientRepository.save(patient);
        String role = scopeService.primaryRole(principal);
        var open = missedVisitService.openTasksForRole(role).stream()
                .filter(t -> t.getPatientId().equals(patient.getId()) && "OPEN".equals(t.getStatus()))
                .findFirst();
        if (open.isPresent()) {
            missedVisitService.completeFieldTask(open.get().getPublicId(), feedback, principal);
        } else {
            eventService.add(patient.getId(), "FIELD_VISIT", nullSafe(feedback), principal);
        }
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('PERM_REFERRAL_WRITE','PERM_PATIENT_WRITE')")
    public PatientDetailDto closeCase(UserPrincipal principal, String publicId, CloseCaseRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setStage(PatientStage.CLOSED);
        patient.setClosedReason(req.reason());
        patientRepository.save(patient);
        eventService.add(patient.getId(), "CLOSED", req.reason() + " · " + nullSafe(req.notes()), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PATIENT_WRITE')")
    public PatientDetailDto monitorVisit(UserPrincipal principal, String publicId, String notes) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        requireStage(patient, PatientStage.MONITOR);
        patient.setCounselled(true);
        patientRepository.save(patient);
        eventService.add(patient.getId(), "MONITOR", "Monitoring visit logged · " + nullSafe(notes), principal);
        return get(principal, publicId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PATIENT_WRITE')")
    public PatientDetailDto addHospitalPatient(UserPrincipal principal, AddHospitalPatientRequest req) {
        UserAccount user = scopeService.requireUser(principal);
        String role = scopeService.primaryRole(principal);
        if (!List.of("HOSP", "ONCO", "MO").contains(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only hospital/MO can direct-register");
        }
        phcRepository.findByCodeAndActiveTrue(req.phcCode())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHC", "Unknown PHC"));

        CancerType type = CancerType.valueOf(req.cancerType().toLowerCase(Locale.ROOT));
        PresentationType presentation = PresentationType.valueOf(req.presentation().toLowerCase(Locale.ROOT));

        Patient patient = new Patient();
        patient.setPublicId(UUID.randomUUID().toString());
        patient.setUhid(nextUhid());
        patient.setFullName(req.fullName().trim());
        patient.setAge(req.age());
        patient.setSex(req.sex());
        patient.setMobile(blankToNull(req.mobile()));
        patient.setAbhaId(blankToNull(req.abhaId()));
        patient.setSmsLang(req.smsLang() == null ? "gu" : req.smsLang());
        patient.setVillage(req.village().trim());
        patient.setPhcCode(req.phcCode());
        patient.setTaluka(user.getTaluka());
        patient.setDistrict(user.getDistrict() != null ? user.getDistrict() : "Gandhinagar");
        patient.setStateCode(user.getStateCode() != null ? user.getStateCode() : "GJ");
        patient.setCancerType(type);
        patient.setPresentation(presentation);
        patient.setFindingNotes(req.findingNotes());
        patient.setScreenedByUserId(user.getId());
        patient.setScreenedByName(user.getFullName());
        patient.setScreenedByRole(role);
        patient.setScreenedAt(Instant.now());
        patient.setStage(PatientStage.BOOKED);
        patient.setRiskLevel(RiskLevel.HIGH);
        patient.setAssignedFacCode(user.getFacilityCode() != null ? user.getFacilityCode() : "DH");
        patient.setVisitAt(LocalDate.now());
        patient.setVisitWindow("OPD walk-in");
        patient.setEntrySource("HOSPITAL");
        patient.setGpsConsent(req.gpsConsent());
        patient.setDataConsent(req.dataConsent());
        patient.setMoAcked(false);
        patient.setWhoClass(whoFor(type));
        patientRepository.save(patient);

        eventService.add(patient.getId(), "HOSP_REGISTER",
                "Direct OPD registration at " + patient.getAssignedFacCode(), principal);
        notifications.alert("Acknowledge hospital registration",
                patient.getFullName() + " registered at hospital from your area",
                "INFO", "MO", patient.getPhcCode(), null, patient);
        if (patient.getMobile() != null) {
            notifications.sms(patient, "CARESETU registration",
                    "CARESETU: You are registered for cancer pathway follow-up. Helpline 104.");
        }
        return get(principal, patient.getPublicId());
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PATIENT_WRITE')")
    public PatientDetailDto saveEligibility(UserPrincipal principal, String publicId, EligibilityRequest req) {
        Patient patient = scopeService.requirePatientInScope(principal, publicId);
        patient.setIncomeBand(req.incomeBand());
        patient.setRationCard(req.rationCard());
        patient.setHasPmjayCard(req.hasPmjayCard());
        patient.setEsic(Boolean.TRUE.equals(req.esic()));
        patient.setGovtEmployee(Boolean.TRUE.equals(req.govtEmployee()));
        patient.setPrivateInsurance(Boolean.TRUE.equals(req.privateInsurance()));
        patientRepository.save(patient);
        eventService.add(patient.getId(), "ELIGIBILITY", "Scheme eligibility updated", principal);
        return get(principal, publicId);
    }

    private static String whoFor(CancerType type) {
        return switch (type) {
            case oral -> "C00–C14 Lip, oral cavity and pharynx";
            case breast -> "C50 Breast";
            case cervical -> "C53 Cervix uteri";
        };
    }

    private String nextUhid() {
        long seq = patientRepository.count() + 1;
        return "CS-GNR-" + String.format("%04d", seq);
    }

    private static void requireStage(Patient patient, PatientStage expected) {
        if (patient.getStage() != expected) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STAGE",
                    "Expected stage " + expected + " but was " + patient.getStage());
        }
    }

    private static String defaultTestName(CancerType type) {
        return switch (type) {
            case oral -> "Biopsy of oral lesion";
            case breast -> "Ultrasound + core biopsy";
            case cervical -> "Colposcopy + biopsy";
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
