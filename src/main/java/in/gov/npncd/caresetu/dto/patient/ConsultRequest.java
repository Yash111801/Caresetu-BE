package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultRequest(
        @NotBlank String outcome,
        @Size(max = 120) String treatmentPlan,
        @Size(max = 500) String notes
) {}
