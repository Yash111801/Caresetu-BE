package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Alert;
import in.gov.npncd.caresetu.domain.entity.MessageLog;
import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.repository.AlertRepository;
import in.gov.npncd.caresetu.repository.MessageLogRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDispatchService {

    private final AlertRepository alertRepository;
    private final MessageLogRepository messageLogRepository;

    public NotificationDispatchService(AlertRepository alertRepository, MessageLogRepository messageLogRepository) {
        this.alertRepository = alertRepository;
        this.messageLogRepository = messageLogRepository;
    }

    @Transactional
    public void alert(String title, String body, String severity, String role, String phc, String fac, Patient patient) {
        Alert alert = new Alert();
        alert.setPublicId(UUID.randomUUID().toString());
        alert.setTitle(title);
        alert.setBody(body);
        alert.setSeverity(severity);
        alert.setAudienceRole(role);
        alert.setAudiencePhc(phc);
        alert.setAudienceFac(fac);
        alert.setPatientId(patient != null ? patient.getId() : null);
        alertRepository.save(alert);
    }

    @Transactional
    public void sms(Patient patient, String subject, String body) {
        if (patient.getMobile() == null || patient.getMobile().isBlank()) {
            return;
        }
        MessageLog msg = new MessageLog();
        msg.setPublicId(UUID.randomUUID().toString());
        msg.setChannel("SMS");
        msg.setDirection("OUT");
        msg.setMobile(patient.getMobile());
        msg.setSubject(subject);
        msg.setBody(body);
        msg.setLanguage("en");
        msg.setPatientId(patient.getId());
        msg.setStatus("SENT");
        messageLogRepository.save(msg);
    }

    @Transactional
    public void ivrs(Patient patient, String subject, String body) {
        if (patient.getMobile() == null || patient.getMobile().isBlank()) {
            return;
        }
        MessageLog msg = new MessageLog();
        msg.setPublicId(UUID.randomUUID().toString());
        msg.setChannel("IVRS");
        msg.setDirection("OUT");
        msg.setMobile(patient.getMobile());
        msg.setSubject(subject);
        msg.setBody(body);
        msg.setLanguage("gu");
        msg.setPatientId(patient.getId());
        msg.setStatus("QUEUED");
        messageLogRepository.save(msg);
    }
}
