package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.alert.AlertDto;
import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.AlertQueryService;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AlertController {

    private final AlertQueryService alertQueryService;

    public AlertController(AlertQueryService alertQueryService) {
        this.alertQueryService = alertQueryService;
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertDto>> alerts(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(alertQueryService.myAlerts(principal));
    }

    @PostMapping("/alerts/{id}/read")
    public ApiResponse<AlertDto> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ApiResponse.ok(alertQueryService.markRead(principal, id));
    }

    @GetMapping("/messages")
    public ApiResponse<List<Map<String, Object>>> messages(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(alertQueryService.messages(principal));
    }
}
