package in.gov.npncd.caresetu.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile must be a valid 10-digit Indian number")
        String mobile,

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
        String otp,

        @Size(max = 128)
        String deviceId
) {
}
