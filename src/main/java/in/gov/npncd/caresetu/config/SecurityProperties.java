package in.gov.npncd.caresetu.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caresetu.security")
public record SecurityProperties(
        Jwt jwt,
        Otp otp,
        Cors cors,
        RateLimit rateLimit
) {
    public record Jwt(String secret, long accessTokenTtlMinutes, long refreshTokenTtlDays) {
    }

    public record Otp(
            int length,
            int ttlMinutes,
            int maxAttempts,
            int resendCooldownSeconds,
            boolean demoOtpEnabled,
            String demoOtp
    ) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record RateLimit(int authRequestsPerMinute) {
    }
}
