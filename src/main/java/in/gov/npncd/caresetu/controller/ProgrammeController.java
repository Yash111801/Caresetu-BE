package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.dto.patient.EligibilityRequest;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.ProgrammeService;
import in.gov.npncd.caresetu.service.SchemeService;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProgrammeController {

    private final SchemeService schemeService;
    private final ProgrammeService programmeService;

    public ProgrammeController(SchemeService schemeService, ProgrammeService programmeService) {
        this.schemeService = schemeService;
        this.programmeService = programmeService;
    }

    @GetMapping("/meta")
    @PreAuthorize("permitAll()")
    public ApiResponse<Map<String, Object>> meta() {
        return ApiResponse.ok(programmeService.meta());
    }

    @GetMapping("/schemes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> schemes() {
        return ApiResponse.ok(schemeService.listSchemes());
    }

    @PostMapping("/schemes/evaluate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> evaluate(@RequestBody EligibilityRequest request) {
        return ApiResponse.ok(schemeService.evaluate(request));
    }

    @GetMapping("/audit/monthly")
    public ApiResponse<Map<String, Object>> audit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer monthKey) {
        return ApiResponse.ok(programmeService.monthlyAudit(principal, monthKey));
    }

    @GetMapping("/intervals")
    public ApiResponse<Map<String, Object>> intervals(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(programmeService.timeIntervals(principal));
    }
}
