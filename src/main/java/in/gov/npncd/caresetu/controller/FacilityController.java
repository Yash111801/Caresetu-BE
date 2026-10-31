package in.gov.npncd.caresetu.controller;

import in.gov.npncd.caresetu.dto.common.ApiResponse;
import in.gov.npncd.caresetu.dto.facility.FacilityDto;
import in.gov.npncd.caresetu.dto.facility.PhcDto;
import in.gov.npncd.caresetu.dto.facility.PoolDayDto;
import in.gov.npncd.caresetu.dto.patient.BookingDto;
import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import in.gov.npncd.caresetu.repository.BookingRepository;
import in.gov.npncd.caresetu.service.FacilityService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class FacilityController {

    private final FacilityService facilityService;
    private final BookingRepository bookingRepository;

    public FacilityController(FacilityService facilityService, BookingRepository bookingRepository) {
        this.facilityService = facilityService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/facilities")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FacilityDto>> facilities() {
        return ApiResponse.ok(facilityService.listFacilities());
    }

    @GetMapping("/phcs")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PhcDto>> phcs() {
        return ApiResponse.ok(facilityService.listPhcs());
    }

    @GetMapping("/facilities/{code}/pool")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PoolDayDto>> pool(
            @PathVariable String code,
            @RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(facilityService.poolDays(code, Math.min(days, 14)));
    }

    @GetMapping("/facilities/{code}/appointments")
    @PreAuthorize("hasAuthority('PERM_APPOINTMENT_READ')")
    public ApiResponse<List<BookingDto>> appointments(
            @PathVariable String code,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(1);
        LocalDate end = to != null ? to : LocalDate.now().plusDays(14);
        var list = bookingRepository
                .findByFacilityCodeAndBookingDateBetweenAndStatusOrderByBookingDateAscPlaceNoAsc(
                        code, start, end, BookingStatus.ACTIVE)
                .stream()
                .map(b -> new BookingDto(
                        b.getPublicId(), b.getFacilityCode(), b.getBookingDate(), b.getWindowLabel(),
                        b.getPlaceNo(), b.getStatus().name(), b.getBookingKind(), b.getBookedByName()))
                .toList();
        return ApiResponse.ok(list);
    }
}
