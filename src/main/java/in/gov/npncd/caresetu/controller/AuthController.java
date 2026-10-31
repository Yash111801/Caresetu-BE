package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.auth.RefreshTokenRequest;
import in.gov.npncd.caresetu.dto.auth.SendOtpRequest;
import in.gov.npncd.caresetu.dto.auth.SendOtpResponse;
import in.gov.npncd.caresetu.dto.auth.TokenResponse;
import in.gov.npncd.caresetu.dto.auth.UserProfileDto;
import in.gov.npncd.caresetu.dto.auth.VerifyOtpRequest;
import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.mapper.UserMapper;
import in.gov.npncd.caresetu.repository.UserAccountRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserAccountRepository userAccountRepository;
    private final UserMapper userMapper;

    public AuthController(
            AuthService authService,
            UserAccountRepository userAccountRepository,
            UserMapper userMapper) {
        this.authService = authService;
        this.userAccountRepository = userAccountRepository;
        this.userMapper = userMapper;
    }

    @PostMapping("/otp/send")
    public ApiResponse<SendOtpResponse> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok("OTP sent", authService.sendOtp(request, clientIp(httpRequest)));
    }

    @PostMapping("/otp/verify")
    public ApiResponse<TokenResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.verifyOtp(
                request, clientIp(httpRequest), httpRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.refresh(
                request, clientIp(httpRequest), httpRequest.getHeader(HttpHeaders.USER_AGENT)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        authService.logout(
                principal,
                request == null ? null : request.refreshToken(),
                clientIp(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT));
        return ApiResponse.ok("Logged out", null);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileDto profile = userAccountRepository.findByPublicIdAndActiveTrue(principal.getPublicId())
                .map(user -> userMapper.toProfile(principal, user))
                .orElseGet(() -> new UserProfileDto(
                        principal.getPublicId(),
                        principal.getFullName(),
                        "******",
                        null, null, null, null, null,
                        principal.getRoles(),
                        principal.getPermissions()));
        return ApiResponse.ok(profile);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
