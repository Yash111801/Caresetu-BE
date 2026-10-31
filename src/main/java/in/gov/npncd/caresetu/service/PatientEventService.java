package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.PatientEvent;
import in.gov.npncd.caresetu.repository.PatientEventRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientEventService {

    private final PatientEventRepository patientEventRepository;

    public PatientEventService(PatientEventRepository patientEventRepository) {
        this.patientEventRepository = patientEventRepository;
    }

    @Transactional
    public void add(Long patientId, String type, String message, UserPrincipal principal) {
        PatientEvent event = new PatientEvent();
        event.setPatientId(patientId);
        event.setEventType(type);
        event.setMessage(message);
        if (principal != null) {
            event.setActorUserId(principal.getId());
            event.setActorName(principal.getFullName());
            event.setActorRole(principal.getRoles().stream().findFirst().orElse(null));
        }
        patientEventRepository.save(event);
    }
}
