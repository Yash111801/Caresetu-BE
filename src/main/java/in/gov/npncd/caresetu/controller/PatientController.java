package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.dto.patient.AddHospitalPatientRequest;
import in.gov.npncd.caresetu.dto.patient.ApproveScreeningRequest;
import in.gov.npncd.caresetu.dto.patient.AttendVisitRequest;
import in.gov.npncd.caresetu.dto.patient.CloseCaseRequest;
import in.gov.npncd.caresetu.dto.patient.ConsultRequest;
import in.gov.npncd.caresetu.dto.patient.CreateScreeningRequest;
import in.gov.npncd.caresetu.dto.patient.EligibilityRequest;
import in.gov.npncd.caresetu.dto.patient.HomeDto;
import in.gov.npncd.caresetu.dto.patient.PatientDetailDto;
import in.gov.npncd.caresetu.dto.patient.PatientSummaryDto;
import in.gov.npncd.caresetu.dto.patient.ReferBookRequest;
import in.gov.npncd.caresetu.dto.patient.ReportTestRequest;
import in.gov.npncd.caresetu.security.UserPrincipal;
import in.gov.npncd.caresetu.service.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeDto> home(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(patientService.home(principal));
    }

    @GetMapping("/patients")
    public ApiResponse<List<PatientSummaryDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok(patientService.list(principal, stage, q));
    }

    @GetMapping("/patients/{id}")
    public ApiResponse<PatientDetailDto> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ApiResponse.ok(patientService.get(principal, id));
    }

    @PostMapping("/screenings")
    public ApiResponse<PatientDetailDto> createScreening(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateScreeningRequest request) {
        return ApiResponse.ok("Screening created", patientService.createScreening(principal, request));
    }

    @PostMapping("/patients/{id}/cho-review")
    public ApiResponse<PatientDetailDto> choReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ApproveScreeningRequest request) {
        return ApiResponse.ok(patientService.approveByCho(principal, id, request));
    }

    @PostMapping("/patients/{id}/refer")
    public ApiResponse<PatientDetailDto> refer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ReferBookRequest request) {
        return ApiResponse.ok(patientService.referAndBook(principal, id, request));
    }

    @PostMapping("/patients/{id}/attend")
    public ApiResponse<PatientDetailDto> attend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody AttendVisitRequest request) {
        return ApiResponse.ok(patientService.attend(principal, id, request));
    }

    @PostMapping("/patients/{id}/report")
    public ApiResponse<PatientDetailDto> report(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ReportTestRequest request) {
        return ApiResponse.ok(patientService.reportTest(principal, id, request));
    }

    @PostMapping("/patients/{id}/oncology-book")
    public ApiResponse<PatientDetailDto> oncologyBook(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ReferBookRequest request) {
        return ApiResponse.ok(patientService.bookOncology(principal, id, request));
    }

    @PostMapping("/patients/{id}/consult")
    public ApiResponse<PatientDetailDto> consult(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ConsultRequest request) {
        return ApiResponse.ok(patientService.consult(principal, id, request));
    }

    @PostMapping("/patients/{id}/close")
    public ApiResponse<PatientDetailDto> close(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody CloseCaseRequest request) {
        return ApiResponse.ok(patientService.closeCase(principal, id, request));
    }

    @PostMapping("/patients/{id}/monitor")
    public ApiResponse<PatientDetailDto> monitor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body == null ? null : body.get("notes");
        return ApiResponse.ok(patientService.monitorVisit(principal, id, notes));
    }

    @PostMapping("/patients/hospital")
    public ApiResponse<PatientDetailDto> addHospital(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddHospitalPatientRequest request) {
        return ApiResponse.ok("Patient registered", patientService.addHospitalPatient(principal, request));
    }

    @PostMapping("/patients/{id}/eligibility")
    public ApiResponse<PatientDetailDto> eligibility(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody EligibilityRequest request) {
        return ApiResponse.ok(patientService.saveEligibility(principal, id, request));
    }

    @PostMapping("/patients/{id}/ack")
    public ApiResponse<PatientDetailDto> ack(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(patientService.acknowledge(principal, id, body == null ? null : body.get("notes")));
    }

    @PostMapping("/patients/{id}/inconclusive")
    public ApiResponse<PatientDetailDto> inconclusive(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(patientService.markInconclusive(principal, id, body == null ? null : body.get("notes")));
    }

    @PostMapping("/patients/{id}/treatment-start")
    public ApiResponse<PatientDetailDto> txStart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(patientService.startTreatment(
                principal, id,
                body == null ? null : body.get("plan"),
                body == null ? null : body.get("notes")));
    }

    @PostMapping("/patients/{id}/exit")
    public ApiResponse<PatientDetailDto> exit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return ApiResponse.ok(patientService.exitPathway(
                principal, id, body.getOrDefault("reason", "Other"), body.get("notes")));
    }

    @PostMapping("/patients/{id}/field-visit")
    public ApiResponse<PatientDetailDto> fieldVisit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(patientService.fieldVisit(
                principal, id, body == null ? null : body.get("feedback")));
    }
}
