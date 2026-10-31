package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.common.ApiResponse;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample protected endpoint demonstrating RBAC with permissions.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/secure/ping")
    @PreAuthorize("hasAuthority('PERM_AUTH_LOGIN')")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.ok(Map.of("status", "ok"));
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> adminPing() {
        return ApiResponse.ok(Map.of("status", "admin-ok"));
    }
}
