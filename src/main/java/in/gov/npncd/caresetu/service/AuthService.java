package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.config.SecurityProperties;
import in.gov.npncd.caresetu.domain.entity.OtpChallenge;
import in.gov.npncd.caresetu.domain.entity.RefreshToken;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.dto.auth.RefreshTokenRequest;
import in.gov.npncd.caresetu.dto.auth.SendOtpRequest;
import in.gov.npncd.caresetu.dto.auth.SendOtpResponse;
import in.gov.npncd.caresetu.dto.auth.TokenResponse;
import in.gov.npncd.caresetu.dto.auth.VerifyOtpRequest;
import in.gov.npncd.caresetu.exception.ApiException;
import in.gov.npncd.caresetu.mapper.UserMapper;
import in.gov.npncd.caresetu.repository.OtpChallengeRepository;
import in.gov.npncd.caresetu.repository.RefreshTokenRepository;
import in.gov.npncd.caresetu.repository.UserAccountRepository;
import in.gov.npncd.caresetu.security.JwtService;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserAccountRepository userAccountRepository,
            OtpChallengeRepository otpChallengeRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityProperties securityProperties,
            UserMapper userMapper,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request, String ip) {
        UserAccount user = userAccountRepository.findByMobileAndActiveTrue(request.mobile())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "No active user found for this mobile number"));

        if (user.isLocked()) {
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
                    "Account temporarily locked. Try again later.");
        }

        Instant now = Instant.now();
        otpChallengeRepository.findFirstByMobileAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        request.mobile(), now)
                .ifPresent(existing -> {
                    Instant cooldownEnd = existing.getCreatedAt()
                            .plusSeconds(securityProperties.otp().resendCooldownSeconds());
                    if (cooldownEnd.isAfter(now)) {
                        throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_COOLDOWN",
                                "Please wait before requesting another OTP");
                    }
                });

        otpChallengeRepository.consumeAllActive(request.mobile());

        String otp = generateOtp();
        OtpChallenge challenge = new OtpChallenge();
        challenge.setMobile(request.mobile());
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setExpiresAt(now.plus(securityProperties.otp().ttlMinutes(), ChronoUnit.MINUTES));
        challenge.setRequestIp(ip);
        otpChallengeRepository.save(challenge);

        // Production: send via SMS/IVRS gateway (TRAI DLT). Never log OTP.
        auditService.record(user.getId(), "OTP_SENT", "auth", user.getPublicId(), ip, null, true, null);

        return new SendOtpResponse(
                mask(request.mobile()),
                securityProperties.otp().ttlMinutes() * 60,
                securityProperties.otp().resendCooldownSeconds());
    }

    @Transactional
    public TokenResponse verifyOtp(VerifyOtpRequest request, String ip, String userAgent) {
        UserAccount user = userAccountRepository.findByMobileAndActiveTrue(request.mobile())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Invalid mobile or OTP"));

        if (user.isLocked()) {
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
                    "Account temporarily locked. Try again later.");
        }

        OtpChallenge challenge = otpChallengeRepository
                .findFirstByMobileAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        request.mobile(), Instant.now())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "OTP_EXPIRED",
                        "OTP expired or not found. Request a new one."));

        if (challenge.getAttempts() >= securityProperties.otp().maxAttempts()) {
            challenge.setConsumed(true);
            lockTemporarily(user);
            auditService.record(user.getId(), "OTP_MAX_ATTEMPTS", "auth", user.getPublicId(),
                    ip, userAgent, false, null);
            throw new ApiException(HttpStatus.LOCKED, "OTP_LOCKED",
                    "Too many invalid OTP attempts. Account locked temporarily.");
        }

        boolean match = passwordEncoder.matches(request.otp(), challenge.getOtpHash());
        if (!match) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= securityProperties.otp().maxAttempts()) {
                lockTemporarily(user);
            }
            auditService.record(user.getId(), "OTP_FAILED", "auth", user.getPublicId(),
                    ip, userAgent, false, null);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "Invalid mobile or OTP");
        }

        challenge.setConsumed(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());

        // Single-device style: revoke previous refresh tokens
        refreshTokenRepository.revokeAllForUser(user.getId());

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.createAccessToken(principal);
        String refreshRaw = UUID.randomUUID() + "." + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(sha256(refreshRaw));
        refreshToken.setDeviceId(request.deviceId());
        refreshToken.setExpiresAt(Instant.now()
                .plus(securityProperties.jwt().refreshTokenTtlDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        auditService.record(user.getId(), "LOGIN_SUCCESS", "auth", user.getPublicId(),
                ip, userAgent, true, null);

        return new TokenResponse(
                accessToken,
                refreshRaw,
                "Bearer",
                jwtService.accessTokenTtlSeconds(),
                userMapper.toProfile(user));
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request, String ip, String userAgent) {
        String hash = sha256(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH",
                        "Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "REFRESH_EXPIRED",
                    "Refresh token expired");
        }

        UserAccount user = stored.getUser();
        if (!user.isActive() || user.isLocked()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ACCOUNT_DISABLED",
                    "Account is not available");
        }

        stored.setRevoked(true);
        String refreshRaw = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken next = new RefreshToken();
        next.setUser(user);
        next.setTokenHash(sha256(refreshRaw));
        next.setDeviceId(stored.getDeviceId());
        next.setExpiresAt(Instant.now()
                .plus(securityProperties.jwt().refreshTokenTtlDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(next);

        UserPrincipal principal = new UserPrincipal(user);
        auditService.record(user.getId(), "TOKEN_REFRESH", "auth", user.getPublicId(),
                ip, userAgent, true, null);

        return new TokenResponse(
                jwtService.createAccessToken(principal),
                refreshRaw,
                "Bearer",
                jwtService.accessTokenTtlSeconds(),
                userMapper.toProfile(user));
    }

    @Transactional
    public void logout(UserPrincipal principal, String refreshTokenRaw, String ip, String userAgent) {
        if (refreshTokenRaw != null && !refreshTokenRaw.isBlank()) {
            refreshTokenRepository.findByTokenHashAndRevokedFalse(sha256(refreshTokenRaw))
                    .ifPresent(token -> token.setRevoked(true));
        } else {
            refreshTokenRepository.revokeAllForUser(principal.getId());
        }
        auditService.record(principal.getId(), "LOGOUT", "auth", principal.getPublicId(),
                ip, userAgent, true, null);
    }

    private String generateOtp() {
        if (securityProperties.otp().demoOtpEnabled()) {
            return securityProperties.otp().demoOtp();
        }
        int bound = (int) Math.pow(10, securityProperties.otp().length());
        int otp = secureRandom.nextInt(bound / 10, bound);
        return String.valueOf(otp);
    }

    private void lockTemporarily(UserAccount user) {
        user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
    }

    private static String mask(String mobile) {
        return "******" + mobile.substring(mobile.length() - 4);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }
}
