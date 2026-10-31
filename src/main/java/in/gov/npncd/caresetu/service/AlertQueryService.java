package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Alert;
import in.gov.npncd.caresetu.domain.entity.MessageLog;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.dto.alert.AlertDto;
import in.gov.npncd.caresetu.exception.ApiException;
import in.gov.npncd.caresetu.repository.AlertRepository;
import in.gov.npncd.caresetu.repository.MessageLogRepository;
import in.gov.npncd.caresetu.repository.PatientRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertQueryService {

    private final AlertRepository alertRepository;
    private final MessageLogRepository messageLogRepository;
    private final PatientRepository patientRepository;
    private final ScopeService scopeService;

    public AlertQueryService(
            AlertRepository alertRepository,
            MessageLogRepository messageLogRepository,
            PatientRepository patientRepository,
            ScopeService scopeService) {
        this.alertRepository = alertRepository;
        this.messageLogRepository = messageLogRepository;
        this.patientRepository = patientRepository;
        this.scopeService = scopeService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_ALERT_READ')")
    public List<AlertDto> myAlerts(UserPrincipal principal) {
        UserAccount user = scopeService.requireUser(principal);
        String role = scopeService.primaryRole(principal);
        return alertRepository.findForAudience(role, user.getPhcCode(), user.getFacilityCode()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_ALERT_READ')")
    public AlertDto markRead(UserPrincipal principal, String publicId) {
        Alert alert = alertRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ALERT_NOT_FOUND", "Alert not found"));
        alert.setReadFlag(true);
        return toDto(alert);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> messages(UserPrincipal principal) {
        return messageLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::toMessage)
                .toList();
    }

    private AlertDto toDto(Alert a) {
        String patientPublicId = null;
        if (a.getPatientId() != null) {
            patientPublicId = patientRepository.findById(a.getPatientId())
                    .map(p -> p.getPublicId()).orElse(null);
        }
        return new AlertDto(a.getPublicId(), a.getTitle(), a.getBody(), a.getSeverity(),
                patientPublicId, a.isReadFlag(), a.getCreatedAt());
    }

    private Map<String, Object> toMessage(MessageLog m) {
        return Map.of(
                "id", m.getPublicId(),
                "channel", m.getChannel(),
                "mobile", m.getMobile() == null ? "" : "******" + m.getMobile().substring(Math.max(0, m.getMobile().length() - 4)),
                "subject", m.getSubject() == null ? "" : m.getSubject(),
                "body", m.getBody(),
                "status", m.getStatus(),
                "language", m.getLanguage(),
                "createdAt", m.getCreatedAt() == null ? "" : m.getCreatedAt().toString());
    }
}
