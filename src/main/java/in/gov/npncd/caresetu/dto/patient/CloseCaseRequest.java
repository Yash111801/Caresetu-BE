package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CloseCaseRequest(
        @NotBlank @Size(max = 120) String reason,
        @Size(max = 500) String notes
) {}
