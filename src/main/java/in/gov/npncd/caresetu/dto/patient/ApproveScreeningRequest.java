package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApproveScreeningRequest(
        @NotBlank String outcome,
        @Size(max = 500) String notes
) {}
