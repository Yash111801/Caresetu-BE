package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.dto.patient.WorkActionDto;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorklistService {

    private final ScopeService scopeService;
    private final MissedVisitService missedVisitService;

    public WorklistService(ScopeService scopeService, MissedVisitService missedVisitService) {
        this.scopeService = scopeService;
        this.missedVisitService = missedVisitService;
    }

    public List<WorkActionDto> actionsFor(Patient p, UserPrincipal principal) {
        String role = scopeService.primaryRole(principal);
        List<WorkActionDto> actions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Map<String, Object> miss = missedVisitService.missStatus(p.getId());

        if ("ASHA".equals(role) && p.getStage() == PatientStage.MONITOR) {
            actions.add(new WorkActionDto("monitor", "Counsel / monitor",
                    "Asymptomatic pathway · monitor until " + (p.getMonitorUntil() != null ? p.getMonitorUntil() : "—"),
                    null));
        }
        if ("CHO".equals(role) && p.getStage() == PatientStage.CHO) {
            actions.add(new WorkActionDto("approve", "Review and forward",
                    "Classify and forward to PHC MO", null));
        }
        if ("MO".equals(role)) {
            if (p.getStage() == PatientStage.REVIEW) {
                actions.add(new WorkActionDto("refer", "Approve and book",
                        "Book green-channel place at hospital", null));
            }
            if (p.getStage() == PatientStage.ONCO_REQ) {
                actions.add(new WorkActionDto("obook", "Book oncology",
                        "Cancer confirmed · book first oncology consultation", null));
            }
            if (p.getStage() == PatientStage.MISSED || p.getStage() == PatientStage.ONCO_MISSED) {
                actions.add(new WorkActionDto("rebook", "Rebook or close",
                        "Missed visit · rebook hospital or close pathway",
                        Boolean.TRUE.equals(miss.get("escalated")) ? "esc" : "fb"));
            }
            if ("HOSPITAL".equalsIgnoreCase(p.getEntrySource()) && !p.isMoAcked()
                    && p.getStage() != PatientStage.CLOSED) {
                actions.add(new WorkActionDto("ack", "Acknowledge",
                        "Hospital OPD registration from your area", null));
            }
        }
        if ("HOSP".equals(role) && p.getStage() == PatientStage.BOOKED
                && p.getVisitAt() != null && !p.getVisitAt().isAfter(today)) {
            actions.add(new WorkActionDto("attend", "Record visit",
                    "Green-channel visit due · record attendance / tests", null));
            actions.add(new WorkActionDto("cantserve", "Could not serve",
                    "Equipment / specialist / session issue", "warn"));
        }
        if ("PATH".equals(role) && p.getStage() == PatientStage.TESTED) {
            actions.add(new WorkActionDto("report", "Update tests", "Enter diagnostic report", null));
            actions.add(new WorkActionDto("inconclusive", "Mark inconclusive",
                    "Repeat test required", "warn"));
        }
        if ("ONCO".equals(role) && p.getStage() == PatientStage.SPECIALIST
                && p.getOncoVisitAt() != null && !p.getOncoVisitAt().isAfter(today)) {
            actions.add(new WorkActionDto("consult", "Record consultation",
                    "First oncology consultation", null));
        }
        if (("ONCO".equals(role) || "MO".equals(role))
                && p.getStage() == PatientStage.CLOSED
                && "POSITIVE".equalsIgnoreCase(p.getTestResult())
                && !p.isTreatmentStarted()) {
            actions.add(new WorkActionDto("txstart", "Start treatment",
                    "Mark treatment started", null));
        }
        if (List.of("MO", "HOSP", "ONCO", "CHO").contains(role) && p.getStage() != PatientStage.CLOSED) {
            actions.add(new WorkActionDto("exit", "Exit pathway",
                    "Refuse / died / moved / other", null));
        }
        boolean needField = false;
        String fieldLabel = "Record contact";
        String fieldWhat = "Field contact needed";
        if (("ASHA".equals(role) || "CHO".equals(role))
                && Boolean.TRUE.equals(miss.get("failed"))
                && miss.get("fieldFeedback") == null) {
            needField = true;
            fieldLabel = "Missed-visit field follow-up";
            fieldWhat = "3 voice calls not answered · record feedback";
        } else if (("ASHA".equals(role) || "CHO".equals(role))
                && (p.getMobile() == null || p.getMobile().isBlank())
                && p.getStage() != PatientStage.CLOSED
                && p.getInformedAt() == null) {
            needField = true;
            fieldLabel = "Record contact";
            fieldWhat = "No mobile number · field contact needed";
        }
        if (needField) {
            actions.add(new WorkActionDto("fieldvisit", fieldLabel, fieldWhat, "field"));
        }
        return actions;
    }
}
