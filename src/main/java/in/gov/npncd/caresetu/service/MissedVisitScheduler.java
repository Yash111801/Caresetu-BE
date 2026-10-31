package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.PatientMiss;
import in.gov.npncd.caresetu.repository.PatientMissRepository;
import in.gov.npncd.caresetu.repository.PatientRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MissedVisitScheduler {

    private final PatientMissRepository missRepository;
    private final PatientRepository patientRepository;
    private final MissedVisitService missedVisitService;

    public MissedVisitScheduler(
            PatientMissRepository missRepository,
            PatientRepository patientRepository,
            MissedVisitService missedVisitService) {
        this.missRepository = missRepository;
        this.patientRepository = patientRepository;
        this.missedVisitService = missedVisitService;
    }

    @Scheduled(fixedDelayString = "${caresetu.miss-ladder-ms:60000}")
    @Transactional
    public void tickOpenMisses() {
        List<PatientMiss> open = missRepository.findByResolvedAtIsNull();
        for (PatientMiss miss : open) {
            Patient patient = patientRepository.findById(miss.getPatientId()).orElse(null);
            if (patient != null) {
                missedVisitService.runLadderTick(patient);
            }
        }
    }
}
