package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportTestRequest(
        @NotNull Long testId,
        @NotBlank String resultCode,
        @Size(max = 500) String resultNotes
) {}
