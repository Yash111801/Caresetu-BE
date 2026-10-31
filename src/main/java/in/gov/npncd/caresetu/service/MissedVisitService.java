package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.ContactAttempt;
import in.gov.npncd.caresetu.domain.entity.FieldTask;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.PatientMiss;
import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.exception.ApiException;
import in.gov.npncd.caresetu.repository.BookingRepository;
import in.gov.npncd.caresetu.repository.ContactAttemptRepository;
import in.gov.npncd.caresetu.repository.FieldTaskRepository;
import in.gov.npncd.caresetu.repository.PatientMissRepository;
import in.gov.npncd.caresetu.repository.PatientRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissedVisitService {

    private final PatientMissRepository missRepository;
    private final FieldTaskRepository fieldTaskRepository;
    private final ContactAttemptRepository contactAttemptRepository;
    private final BookingRepository bookingRepository;
    private final PatientRepository patientRepository;
    private final PatientEventService eventService;
    private final NotificationDispatchService notifications;

    public MissedVisitService(
            PatientMissRepository missRepository,
            FieldTaskRepository fieldTaskRepository,
            ContactAttemptRepository contactAttemptRepository,
            BookingRepository bookingRepository,
            PatientRepository patientRepository,
            PatientEventService eventService,
            NotificationDispatchService notifications) {
        this.missRepository = missRepository;
        this.fieldTaskRepository = fieldTaskRepository;
        this.contactAttemptRepository = contactAttemptRepository;
        this.bookingRepository = bookingRepository;
        this.patientRepository = patientRepository;
        this.eventService = eventService;
        this.notifications = notifications;
    }

    @Transactional
    public PatientMiss startMiss(Patient patient, boolean onco, UserPrincipal principal) {
        bookingRepository.findFirstByPatientIdAndStatusOrderByCreatedAtDesc(patient.getId(), BookingStatus.ACTIVE)
                .ifPresent(b -> {
                    b.setStatus(BookingStatus.MISSED);
                    bookingRepository.save(b);
                });

        LocalDate missedAt = onco
                ? (patient.getOncoVisitAt() != null ? patient.getOncoVisitAt() : LocalDate.now())
                : (patient.getVisitAt() != null ? patient.getVisitAt() : LocalDate.now());

        PatientMiss miss = new PatientMiss();
        miss.setPatientId(patient.getId());
        miss.setFacilityCode(onco ? "RCC" : (patient.getAssignedFacCode() != null ? patient.getAssignedFacCode() : "DH"));
        miss.setMissedAt(missedAt);
        miss.setOnco(onco);
        miss.setEscalateAt(missedAt.plusDays(7));
        miss.setCall1Answered(false);
        miss.setCall2Answered(false);
        miss.setCall3Answered(false);
        missRepository.save(miss);

        patient.setStage(onco ? PatientStage.ONCO_MISSED : PatientStage.MISSED);
        patientRepository.save(patient);

        // Contact ladder day 0: IVRS + SMS
        addContact(patient.getId(), "IVRS", "Unanswered", 1, "Missed-visit ladder auto call");
        addContact(patient.getId(), "SMS", "Delivered", 1, "Missed-visit reminder SMS");
        notifications.sms(patient, "CARESETU missed visit",
                "CARESETU: You missed your hospital visit. Please contact your ASHA or call 104.");
        notifications.ivrs(patient, "Missed visit IVRS", "Voice call queued for missed visit.");
        notifications.alert("Missed visit", patient.getFullName() + " missed " + miss.getFacilityCode(),
                "CRIT", "MO", patient.getPhcCode(), null, patient);

        eventService.add(patient.getId(), "MISSED",
                "Missed visit at " + miss.getFacilityCode() + " · contact ladder started", principal);
        return miss;
    }

    @Transactional
    public void runLadderTick(Patient patient) {
        PatientMiss miss = missRepository.findFirstByPatientIdAndResolvedAtIsNullOrderByMissedAtDesc(patient.getId())
                .orElse(null);
        if (miss == null) {
            return;
        }
        long day = ChronoUnit.DAYS.between(miss.getMissedAt(), LocalDate.now());
        if (day >= 1 && contactAttemptRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .noneMatch(c -> "IVRS".equals(c.getChannel()) && c.getAttemptNo() == 2)) {
            addContact(patient.getId(), "IVRS", "Unanswered", 2, "Ladder day 1");
        }
        if (day >= 3 && contactAttemptRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .noneMatch(c -> "IVRS".equals(c.getChannel()) && c.getAttemptNo() == 3)) {
            addContact(patient.getId(), "IVRS", "Unanswered", 3, "Ladder day 3 · failed");
            ensureFieldTask(patient, "miss",
                    "Missed " + miss.getFacilityCode() + " visit · 3 voice calls not answered · visit or call and record feedback",
                    "ASHA");
        }
        if (day >= 7 && miss.getActionAt() == null) {
            notifications.alert("Missed visit escalated", patient.getFullName() + " open >7 days",
                    "CRIT", "MO", patient.getPhcCode(), null, patient);
        }
        if (patient.getMobile() == null || patient.getMobile().isBlank()) {
            ensureFieldTask(patient, "nophone",
                    "No mobile number · inform patient about appointment / missed visit", "ASHA");
        }
    }

    @Transactional
    public void completeFieldTask(String taskId, String feedback, UserPrincipal principal) {
        FieldTask task = fieldTaskRepository.findByPublicId(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "Field task not found"));
        task.setStatus("DONE");
        task.setFeedback(feedback);
        task.setCompletedAt(Instant.now());
        fieldTaskRepository.save(task);

        Patient patient = patientRepository.findById(task.getPatientId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found"));
        if ("nophone".equals(task.getKind())) {
            patient.setInformedAt(Instant.now());
            patientRepository.save(patient);
        }
        if ("miss".equals(task.getKind())) {
            missRepository.findFirstByPatientIdAndResolvedAtIsNullOrderByMissedAtDesc(patient.getId())
                    .ifPresent(m -> {
                        m.setFieldFeedback(feedback);
                        m.setFieldAt(Instant.now());
                        missRepository.save(m);
                    });
        }
        eventService.add(patient.getId(), "FIELD_VISIT",
                task.getKind() + " · " + (feedback == null ? "" : feedback), principal);
    }

    @Transactional
    public void resolveMiss(Patient patient, String how, UserPrincipal principal) {
        missRepository.findFirstByPatientIdAndResolvedAtIsNullOrderByMissedAtDesc(patient.getId())
                .ifPresent(m -> {
                    m.setResolvedAt(Instant.now());
                    m.setResolveHow(how);
                    m.setActionAt(Instant.now());
                    missRepository.save(m);
                });
        eventService.add(patient.getId(), "MISS_RESOLVED", how, principal);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> missStatus(Long patientId) {
        return missRepository.findFirstByPatientIdAndResolvedAtIsNullOrderByMissedAtDesc(patientId)
                .map(m -> {
                    long day = Math.max(0, ChronoUnit.DAYS.between(m.getMissedAt(), LocalDate.now()));
                    int tried = day >= 3 ? 3 : day >= 1 ? 2 : 1;
                    boolean answered = (tried >= 1 && m.isCall1Answered())
                            || (tried >= 2 && m.isCall2Answered())
                            || (tried >= 3 && m.isCall3Answered());
                    Map<String, Object> map = new HashMap<>();
                    map.put("day", day);
                    map.put("tried", tried);
                    map.put("answered", answered);
                    map.put("failed", day >= 3 && !answered);
                    map.put("escalated", day >= 7);
                    map.put("facilityCode", m.getFacilityCode());
                    map.put("onco", m.isOnco());
                    map.put("fieldFeedback", m.getFieldFeedback());
                    return map;
                }).orElse(Map.of());
    }

    @Transactional(readOnly = true)
    public List<FieldTask> openTasksForRole(String role) {
        return fieldTaskRepository.findByAssigneeRoleAndStatusOrderByCreatedAtDesc(role, "OPEN");
    }

    private void ensureFieldTask(Patient patient, String kind, String text, String role) {
        boolean exists = fieldTaskRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .anyMatch(t -> "OPEN".equals(t.getStatus()) && kind.equals(t.getKind()));
        if (exists) {
            return;
        }
        FieldTask task = new FieldTask();
        task.setPublicId(UUID.randomUUID().toString());
        task.setPatientId(patient.getId());
        task.setAssigneeRole(role);
        task.setKind(kind);
        task.setTaskText(text);
        task.setStatus("OPEN");
        fieldTaskRepository.save(task);
    }

    private void addContact(Long patientId, String channel, String status, int attemptNo, String notes) {
        ContactAttempt c = new ContactAttempt();
        c.setPatientId(patientId);
        c.setChannel(channel);
        c.setStatus(status);
        c.setAttemptNo(attemptNo);
        c.setNotes(notes);
        contactAttemptRepository.save(c);
    }
}
