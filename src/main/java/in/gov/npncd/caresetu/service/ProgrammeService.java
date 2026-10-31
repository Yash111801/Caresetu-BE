package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.AuditException;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.repository.AuditExceptionRepository;
import in.gov.npncd.caresetu.repository.PatientRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgrammeService {

    private final AuditExceptionRepository auditExceptionRepository;
    private final ScopeService scopeService;
    private final PatientRepository patientRepository;

    public ProgrammeService(
            AuditExceptionRepository auditExceptionRepository,
            ScopeService scopeService,
            PatientRepository patientRepository) {
        this.auditExceptionRepository = auditExceptionRepository;
        this.scopeService = scopeService;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('PERM_AUDIT_READ','PERM_DASHBOARD_DISTRICT','PERM_DASHBOARD_STATE','PERM_DASHBOARD_NATIONAL') or hasAnyRole('DIST','SNO','NAT','ADMIN','BLK')")
    public Map<String, Object> monthlyAudit(UserPrincipal principal, Integer monthKey) {
        LocalDate now = LocalDate.now();
        int key = monthKey != null ? monthKey : (now.getYear() * 12 + now.getMonthValue() - 2);
        List<AuditException> rows = auditExceptionRepository.findByMonthKeyOrderByStatusAscAreaNameAsc(key);
        Map<String, Long> counts = new HashMap<>();
        counts.put("exceptions", (long) rows.size());
        counts.put("open", rows.stream().filter(r -> "Open".equalsIgnoreCase(r.getStatus())).count());
        counts.put("corrected", rows.stream().filter(r -> "Corrected".equalsIgnoreCase(r.getStatus())).count());
        counts.put("reviewed", rows.stream().filter(r -> "Reviewed".equalsIgnoreCase(r.getStatus())).count());

        List<Map<String, Object>> items = rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getPublicId());
            m.put("areaCode", r.getAreaCode());
            m.put("areaName", r.getAreaName());
            m.put("issueCode", r.getIssueCode());
            m.put("issueText", r.getIssueText());
            m.put("status", r.getStatus());
            m.put("dueDate", r.getDueDate());
            if (r.getPatientId() != null) {
                patientRepository.findById(r.getPatientId()).ifPresent(p -> m.put("patientId", p.getPublicId()));
            }
            return m;
        }).toList();

        Map<String, Object> out = new HashMap<>();
        out.put("monthKey", key);
        out.put("title", "Monthly audit");
        out.put("role", scopeService.primaryRole(principal));
        out.put("counts", counts);
        out.put("rows", items);
        return out;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> timeIntervals(UserPrincipal principal) {
        List<Patient> patients = scopeService.patientsInScope(principal).stream()
                .filter(p -> p.getScreenedAt() != null)
                .toList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Patient p : patients) {
            LocalDate screened = p.getScreenedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            Long toBook = daysBetween(screened, p.getVisitAt());
            Long toReport = daysBetween(screened, p.getReportAt() == null ? null
                    : p.getReportAt().atZone(java.time.ZoneOffset.UTC).toLocalDate());
            Long toOnco = daysBetween(screened, p.getOncoVisitAt());
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.getPublicId());
            row.put("uhid", p.getUhid());
            row.put("name", p.getFullName());
            row.put("type", p.getCancerType().name());
            row.put("stage", p.getStage().name());
            row.put("daysToBooking", toBook);
            row.put("daysToReport", toReport);
            row.put("daysToOncology", toOnco);
            row.put("whoClass", p.getWhoClass());
            rows.add(row);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("rows", rows);
        out.put("summary", Map.of(
                "patients", rows.size(),
                "withBooking", rows.stream().filter(r -> r.get("daysToBooking") != null).count(),
                "withReport", rows.stream().filter(r -> r.get("daysToReport") != null).count(),
                "closed", patients.stream().filter(p -> p.getStage() == PatientStage.CLOSED).count()));
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> meta() {
        return Map.of(
                "app", "CARESETU",
                "version", "2.3-prod",
                "tagline", "Together against cancer",
                "org", "NP-NCD · Health & Family Welfare Department, Gujarat",
                "helplines", List.of(
                        Map.of("name", "Ambulance", "number", "108"),
                        Map.of("name", "Health helpline", "number", "104"),
                        Map.of("name", "PM-JAY", "number", "14555")),
                "languages", List.of("en", "gu", "hi"),
                "poolCap", FacilityService.POOL_CAP,
                "cancerTypes", List.of(
                        Map.of("code", "oral", "test", "Oral visual exam (OVE)", "dx", "Biopsy of oral lesion"),
                        Map.of("code", "breast", "test", "Clinical breast exam (CBE)", "dx", "Ultrasound + core biopsy"),
                        Map.of("code", "cervical", "test", "VIA (acetic acid test)", "dx", "Colposcopy + biopsy")));
    }

    private static Long daysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(from, to);
    }
}
