package in.gov.npncd.caresetu.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileDto user
) {
}
