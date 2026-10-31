package in.gov.npncd.caresetu.dto.patient;

import java.time.Instant;

public record DiagnosticTestDto(Long id, String testName, String status, String resultCode, String resultNotes, Instant reportedAt) {}
