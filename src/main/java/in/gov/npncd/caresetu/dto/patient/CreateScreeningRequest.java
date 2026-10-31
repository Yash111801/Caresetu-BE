package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.*;

public record CreateScreeningRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Min(1) @Max(120) int age,
        @NotBlank @Pattern(regexp = "^(M|F|O)$") String sex,
        @Pattern(regexp = "^([6-9]\\d{9})?$") String mobile,
        @NotBlank @Size(max = 120) String village,
        @NotBlank @Size(max = 16) String phcCode,
        @NotBlank String cancerType,
        @NotBlank String presentation,
        @NotBlank @Size(max = 1000) String findingNotes,
        Boolean counselled
) {}
