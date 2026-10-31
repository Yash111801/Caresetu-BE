package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.dto.facility.FacilityDto;
import in.gov.npncd.caresetu.dto.facility.PhcDto;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.FacilityService;
import in.gov.npncd.caresetu.service.ScopeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/map")
public class MapController {

    private final FacilityService facilityService;
    private final ScopeService scopeService;

    public MapController(FacilityService facilityService, ScopeService scopeService) {
        this.facilityService = facilityService;
        this.scopeService = scopeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_MAP_READ')")
    public ApiResponse<Map<String, Object>> mapData(@AuthenticationPrincipal UserPrincipal principal) {
        List<FacilityDto> facilities = facilityService.listFacilities();
        List<PhcDto> phcs = facilityService.listPhcs();
        List<Map<String, Object>> patients = scopeService.patientsInScope(principal).stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .map(this::patientPoint)
                .toList();
        Map<String, Object> payload = new HashMap<>();
        payload.put("facilities", facilities);
        payload.put("phcs", phcs);
        payload.put("patients", patients);
        payload.put("center", Map.of("lat", 23.2235, "lng", 72.65));
        return ApiResponse.ok(payload);
    }

    private Map<String, Object> patientPoint(Patient p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getPublicId());
        m.put("name", p.getFullName());
        m.put("stage", p.getStage().name());
        m.put("type", p.getCancerType().name());
        m.put("risk", p.getRiskLevel().name());
        m.put("lat", p.getLatitude());
        m.put("lng", p.getLongitude());
        return m;
    }
}
