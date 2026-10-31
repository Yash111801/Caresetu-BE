package in.gov.npncd.caresetu.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReferBookRequest(
        @NotBlank String facilityCode,
        @NotNull LocalDate bookingDate,
        @NotBlank String windowLabel
) {}
