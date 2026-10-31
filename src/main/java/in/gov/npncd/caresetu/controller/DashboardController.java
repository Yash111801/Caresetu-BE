package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.dto.dashboard.DashboardDto;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DashboardDto> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(dashboardService.forCurrentUser(principal));
    }
}
