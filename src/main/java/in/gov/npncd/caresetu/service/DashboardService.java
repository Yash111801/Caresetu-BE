package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.ContactAttempt;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.domain.enums.RiskLevel;
import in.gov.npncd.caresetu.dto.dashboard.DashboardDto;
import in.gov.npncd.caresetu.dto.dashboard.KpiDto;
import in.gov.npncd.caresetu.repository.ContactAttemptRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final Map<String, String[]> KDEF = Map.of(
            "K1", new String[]{"Diagnostic attendance", "Approved pathways with recorded assessment"},
            "K2", new String[]{"Diagnosis completed", "Conclusive diagnosis: cancer confirmed or excluded"},
            "K3", new String[]{"Reports on time", "Reports released by promised date"},
            "K4", new String[]{"First oncology consultation", "Oncology consults completed with plan"},
            "K5", new String[]{"PHC action within 24 h", "Alerts acted within 24 hours"},
            "K6", new String[]{"Booked arrivals served", "Booked arrivals who completed assessment"},
            "K7", new String[]{"SMS delivered", "SMS with delivered status"},
            "K8", new String[]{"IVRS answered", "Voice calls that were answered"});

    private final ScopeService scopeService;
    private final ContactAttemptRepository contactAttemptRepository;

    public DashboardService(
            ScopeService scopeService,
            ContactAttemptRepository contactAttemptRepository) {
        this.scopeService = scopeService;
        this.contactAttemptRepository = contactAttemptRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto forCurrentUser(UserPrincipal principal) {
        String role = scopeService.primaryRole(principal);
        List<Patient> patients = scopeService.patientsInScope(principal);

        Map<String, Long> counts = baseCounts(patients);
        List<KpiDto> kpis = kpis(patients);
        List<Map<String, Object>> highlights = highlights(patients);

        String level;
        String title;
        switch (role) {
            case "ASHA", "CHO" -> {
                level = "FIELD";
                title = "My field dashboard";
                kpis = fieldKpis(patients, role);
            }
            case "MO" -> {
                level = "PHC";
                title = "PHC operations dashboard";
            }
            case "HOSP", "PATH" -> {
                level = "FACILITY";
                title = "Hospital / diagnostic dashboard";
            }
            case "ONCO" -> {
                level = "ONCOLOGY";
                title = "Cancer Centre dashboard";
            }
            case "BLK" -> {
                level = "BLOCK";
                title = "Taluka / block dashboard";
            }
            case "DIST" -> {
                level = "DISTRICT";
                title = "District programme dashboard";
            }
            case "SNO" -> {
                level = "STATE";
                title = "State nodal dashboard";
            }
            case "NAT", "ADMIN" -> {
                level = "NATIONAL";
                title = "National NP-NCD dashboard";
                highlights = nationalHighlights(patients);
            }
            default -> {
                level = "OPS";
                title = "Operations dashboard";
            }
        }

        return new DashboardDto(level, title, counts, kpis, highlights);
    }

    private Map<String, Long> baseCounts(List<Patient> patients) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", (long) patients.size());
        counts.put("monitor", patients.stream().filter(p -> p.getStage() == PatientStage.MONITOR).count());
        counts.put("inReview", patients.stream().filter(p ->
                p.getStage() == PatientStage.CHO || p.getStage() == PatientStage.REVIEW).count());
        counts.put("booked", patients.stream().filter(p -> p.getStage() == PatientStage.BOOKED).count());
        counts.put("tested", patients.stream().filter(p -> p.getStage() == PatientStage.TESTED).count());
        counts.put("onco", patients.stream().filter(p ->
                p.getStage() == PatientStage.ONCO_REQ || p.getStage() == PatientStage.SPECIALIST).count());
        counts.put("missed", patients.stream().filter(p ->
                p.getStage() == PatientStage.MISSED || p.getStage() == PatientStage.ONCO_MISSED).count());
        counts.put("closed", patients.stream().filter(p -> p.getStage() == PatientStage.CLOSED).count());
        counts.put("highRisk", patients.stream().filter(p -> p.getRiskLevel() == RiskLevel.HIGH).count());
        counts.put("treatmentStarted", patients.stream().filter(Patient::isTreatmentStarted).count());
        return counts;
    }

    private List<KpiDto> fieldKpis(List<Patient> patients, String role) {
        long entered = patients.size();
        long booked = patients.stream().filter(p -> p.getAssignedFacCode() != null).count();
        long diagnosed = patients.stream().filter(p -> p.getTestResult() != null).count();
        long missed = patients.stream().filter(p ->
                p.getStage() == PatientStage.MISSED || p.getStage() == PatientStage.ONCO_MISSED).count();
        return List.of(
                new KpiDto("F1", "Cases in scope", String.valueOf(entered), role + " catchment"),
                new KpiDto("F2", "Referred / booked", String.valueOf(booked), pct(booked, entered) + "%"),
                new KpiDto("F3", "Diagnosis done", String.valueOf(diagnosed), pct(diagnosed, entered) + "%"),
                new KpiDto("F4", "Missed visits", String.valueOf(missed), "Need field follow-up"));
    }

    private List<KpiDto> kpis(List<Patient> patients) {
        LocalDate today = LocalDate.now();
        LocalDate groupFrom = today.minusDays(120);
        LocalDate groupTo = today.minusDays(30);

        List<Patient> group = patients.stream()
                .filter(p -> p.getScreenedAt() != null)
                .filter(p -> {
                    LocalDate d = p.getScreenedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
                    return !d.isBefore(groupFrom) && !d.isAfter(groupTo);
                })
                .filter(p -> p.getPresentation() != null)
                .toList();

        long k1n = group.stream().filter(p -> p.getAssessedAt() != null || p.getReportAt() != null
                || p.getStage().ordinal() >= PatientStage.TESTED.ordinal()).count();
        long k2n = group.stream().filter(p -> p.getTestResult() != null
                || (p.getClosedReason() != null && p.getClosedReason().toLowerCase().contains("not cancer"))).count();
        List<Patient> dueReports = patients.stream()
                .filter(p -> p.getReportDue() != null && !p.getReportDue().isAfter(today)
                        && !p.getReportDue().isBefore(groupFrom))
                .toList();
        long k3n = dueReports.stream().filter(p -> p.getReportAt() != null).count();
        List<Patient> onc = group.stream()
                .filter(p -> "POSITIVE".equalsIgnoreCase(p.getTestResult()))
                .toList();
        long k4n = onc.stream().filter(p -> p.getConsultAt() != null || p.isTreatmentStarted()
                || (p.getStage() == PatientStage.CLOSED && p.getTreatmentPlan() != null)).count();
        long missed = patients.stream().filter(p ->
                p.getStage() == PatientStage.MISSED || p.getStage() == PatientStage.ONCO_MISSED).count();
        long k5n = Math.max(0, missed - patients.stream().filter(p ->
                (p.getStage() == PatientStage.MISSED || p.getStage() == PatientStage.ONCO_MISSED)
                        && p.getInformedAt() == null).count());
        long bookedArrivals = patients.stream().filter(p ->
                p.getVisitAt() != null && p.getVisitAt().isBefore(today)
                        && p.getStage() != PatientStage.BOOKED && p.getStage() != PatientStage.MISSED).count();
        long bookedDenom = patients.stream().filter(p ->
                p.getVisitAt() != null && p.getVisitAt().isBefore(today)).count();

        List<ContactAttempt> contacts = patients.stream()
                .flatMap(p -> contactAttemptRepository.findByPatientIdOrderByCreatedAtDesc(p.getId()).stream())
                .toList();
        long smsFinal = contacts.stream().filter(c -> "SMS".equals(c.getChannel())
                && List.of("Delivered", "Failed").contains(c.getStatus())).count();
        long smsOk = contacts.stream().filter(c -> "SMS".equals(c.getChannel())
                && "Delivered".equals(c.getStatus())).count();
        long ivrs = contacts.stream().filter(c -> "IVRS".equals(c.getChannel())).count();
        long ivrsOk = contacts.stream().filter(c -> "IVRS".equals(c.getChannel())
                && "Answered".equals(c.getStatus())).count();

        return List.of(
                kpi("K1", k1n, group.size()),
                kpi("K2", k2n, group.size()),
                kpi("K3", k3n, dueReports.size()),
                kpi("K4", k4n, onc.size()),
                kpi("K5", k5n, Math.max(missed, 1)),
                kpi("K6", bookedArrivals, Math.max(bookedDenom, 1)),
                kpi("K7", smsOk, Math.max(smsFinal, 1)),
                kpi("K8", ivrsOk, Math.max(ivrs, 1)));
    }

    private KpiDto kpi(String code, long n, long d) {
        String[] def = KDEF.get(code);
        Integer pct = d == 0 ? null : (int) Math.round(n * 100.0 / d);
        String value = pct == null ? "—" : pct + "%";
        String hint = n + " / " + d + " · " + def[1];
        return new KpiDto(code, def[0], value, hint);
    }

    private List<Map<String, Object>> highlights(List<Patient> patients) {
        List<Map<String, Object>> out = new ArrayList<>();
        patients.stream()
                .filter(p -> p.getStage() == PatientStage.MISSED
                        || p.getStage() == PatientStage.ONCO_MISSED
                        || p.getRiskLevel() == RiskLevel.HIGH)
                .limit(10)
                .forEach(p -> out.add(Map.of(
                        "id", p.getPublicId(),
                        "name", p.getFullName(),
                        "uhid", p.getUhid(),
                        "stage", p.getStage().name(),
                        "risk", p.getRiskLevel().name(),
                        "type", p.getCancerType().name())));
        return out;
    }

    private List<Map<String, Object>> nationalHighlights(List<Patient> patients) {
        // Demo national tiles from Gujarat pilot volume
        return List.of(
                Map.of("code", "GJ", "name", "Gujarat", "screened", patients.size(),
                        "positive", patients.stream().filter(p -> "POSITIVE".equalsIgnoreCase(p.getTestResult())).count(),
                        "dxPct", 72, "live", true),
                Map.of("code", "MH", "name", "Maharashtra", "screened", 120, "positive", 18, "dxPct", 64, "live", false),
                Map.of("code", "RJ", "name", "Rajasthan", "screened", 80, "positive", 11, "dxPct", 58, "live", false),
                Map.of("code", "MP", "name", "Madhya Pradesh", "screened", 60, "positive", 9, "dxPct", 51, "live", false));
    }

    private static String pct(long n, long d) {
        return d == 0 ? "0" : String.valueOf(Math.round(n * 100.0 / d));
    }
}
