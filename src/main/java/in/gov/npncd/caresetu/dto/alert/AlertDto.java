package in.gov.npncd.caresetu.dto.alert;

import java.time.Instant;

public record AlertDto(String id, String title, String body, String severity, String patientId, boolean read, Instant createdAt) {}
