package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Booking;
import in.gov.npncd.caresetu.domain.entity.DiagnosticTest;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.PatientEvent;
import in.gov.npncd.caresetu.dto.patient.BookingDto;
import in.gov.npncd.caresetu.dto.patient.DiagnosticTestDto;
import in.gov.npncd.caresetu.dto.patient.PatientDetailDto;
import in.gov.npncd.caresetu.dto.patient.PatientEventDto;
import in.gov.npncd.caresetu.dto.patient.PatientSummaryDto;
import in.gov.npncd.caresetu.dto.patient.WorkActionDto;
import in.gov.npncd.caresetu.repository.ContactAttemptRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PatientMapperService {

    private final WorklistService worklistService;
    private final SchemeService schemeService;
    private final MissedVisitService missedVisitService;
    private final ContactAttemptRepository contactAttemptRepository;

    public PatientMapperService(
            WorklistService worklistService,
            SchemeService schemeService,
            MissedVisitService missedVisitService,
            ContactAttemptRepository contactAttemptRepository) {
        this.worklistService = worklistService;
        this.schemeService = schemeService;
        this.missedVisitService = missedVisitService;
        this.contactAttemptRepository = contactAttemptRepository;
    }

    public PatientSummaryDto toSummary(Patient p, UserPrincipal principal) {
        List<WorkActionDto> actions = worklistService.actionsFor(p, principal);
        return new PatientSummaryDto(
                p.getPublicId(), p.getUhid(), p.getFullName(), p.getAge(), p.getSex(),
                mask(p.getMobile()), p.getVillage(), p.getPhcCode(),
                p.getCancerType().name(), p.getPresentation().name(), p.getStage().name(),
                p.getRiskLevel().name(), p.getAssignedFacCode(), p.getVisitAt(), p.getVisitWindow(),
                actions);
    }

    public PatientDetailDto toDetail(
            Patient p,
            UserPrincipal principal,
            List<PatientEvent> events,
            List<Booking> bookings,
            List<DiagnosticTest> tests) {
        Map<String, Object> eligibility = schemeService.evaluate(schemeService.fromPatient(p));
        List<Map<String, Object>> contacts = contactAttemptRepository
                .findByPatientIdOrderByCreatedAtDesc(p.getId()).stream()
                .map(c -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("channel", c.getChannel());
                    row.put("status", c.getStatus());
                    row.put("attemptNo", c.getAttemptNo());
                    row.put("notes", c.getNotes() == null ? "" : c.getNotes());
                    row.put("at", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
                    return row;
                })
                .toList();
        return new PatientDetailDto(
                p.getPublicId(), p.getUhid(), p.getFullName(), p.getAge(), p.getSex(),
                mask(p.getMobile()), p.getAbhaId(), p.getSmsLang(), p.getVillage(), p.getPhcCode(),
                p.getTaluka(), p.getDistrict(), p.getStateCode(), p.getCancerType().name(),
                p.getPresentation().name(), p.getStage().name(), p.getRiskLevel().name(),
                p.getScreenedByName(), p.getScreenedByRole(), p.getScreenedAt(), p.getFindingNotes(),
                p.getAssignedFacCode(), p.getVisitAt(), p.getVisitWindow(), p.getTestFacCode(),
                p.getReportDue(), p.getReportAt(), p.getTestResult(), p.getOncoVisitAt(),
                p.getTreatmentPlan(), p.getClosedReason(), p.isMoAcked(), p.isCounselled(),
                p.getMonitorUntil(), p.getLatitude(), p.getLongitude(),
                p.getIncomeBand(), p.getRationCard(), p.getHasPmjayCard(), p.isEsic(),
                p.isGovtEmployee(), p.isPrivateInsurance(), p.isGpsConsent(), p.isDataConsent(),
                p.getWhoClass(), p.getEntrySource(), p.isTreatmentStarted(), p.getExitReason(),
                p.getInformedAt(), p.getAssessedAt(), p.getConsultAt(),
                worklistService.actionsFor(p, principal),
                events.stream().map(e -> new PatientEventDto(
                        e.getEventType(), e.getMessage(), e.getActorName(), e.getActorRole(), e.getCreatedAt())).toList(),
                bookings.stream().map(b -> new BookingDto(
                        b.getPublicId(), b.getFacilityCode(), b.getBookingDate(), b.getWindowLabel(),
                        b.getPlaceNo(), b.getStatus().name(), b.getBookingKind(), b.getBookedByName())).toList(),
                tests.stream().map(t -> new DiagnosticTestDto(
                        t.getId(), t.getTestName(), t.getStatus(), t.getResultCode(), t.getResultNotes(), t.getReportedAt())).toList(),
                eligibility,
                missedVisitService.missStatus(p.getId()),
                contacts);
    }

    private static String mask(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return null;
        }
        if (mobile.length() < 4) {
            return "****";
        }
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
