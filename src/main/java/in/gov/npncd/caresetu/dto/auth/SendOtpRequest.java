package in.gov.npncd.caresetu.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendOtpRequest(
        @NotBlank(message = "Mobile is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile must be a valid 10-digit Indian number")
        String mobile
) {
}
