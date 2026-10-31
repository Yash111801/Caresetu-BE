package in.gov.npncd.caresetu.dto.auth;

public record SendOtpResponse(
        String mobileMasked,
        int expiresInSeconds,
        int resendCooldownSeconds
) {
}
