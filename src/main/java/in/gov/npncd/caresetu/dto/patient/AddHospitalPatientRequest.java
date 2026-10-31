package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddHospitalPatientRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Min(1) @Max(120) int age,
        @NotBlank @Pattern(regexp = "^(M|F|O)$") String sex,
        @Pattern(regexp = "^([6-9]\\d{9})?$") String mobile,
        @Size(max = 32) String abhaId,
        @NotBlank @Size(max = 120) String village,
        @NotBlank @Size(max = 16) String phcCode,
        @NotBlank String cancerType,
        @NotBlank String presentation,
        @NotBlank @Size(max = 1000) String findingNotes,
        @Pattern(regexp = "^(en|gu|hi)$") String smsLang,
        boolean gpsConsent,
        boolean dataConsent
) {
}
