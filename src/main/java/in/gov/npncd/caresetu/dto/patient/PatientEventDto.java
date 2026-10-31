package in.gov.npncd.caresetu.dto.patient;

import java.time.Instant;

public record PatientEventDto(String eventType, String message, String actorName, String actorRole, Instant at) {}
