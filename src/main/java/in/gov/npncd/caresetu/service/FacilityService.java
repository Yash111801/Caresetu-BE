package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Facility;
import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import in.gov.npncd.caresetu.dto.facility.FacilityDto;
import in.gov.npncd.caresetu.dto.facility.PhcDto;
import in.gov.npncd.caresetu.dto.facility.PoolDayDto;
import in.gov.npncd.caresetu.exception.ApiException;
import in.gov.npncd.caresetu.repository.BookingRepository;
import in.gov.npncd.caresetu.repository.FacilityRepository;
import in.gov.npncd.caresetu.repository.PhcRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacilityService {

    public static final int POOL_CAP = 10;

    private final FacilityRepository facilityRepository;
    private final PhcRepository phcRepository;
    private final BookingRepository bookingRepository;

    public FacilityService(
            FacilityRepository facilityRepository,
            PhcRepository phcRepository,
            BookingRepository bookingRepository) {
        this.facilityRepository = facilityRepository;
        this.phcRepository = phcRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<FacilityDto> listFacilities() {
        return facilityRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PhcDto> listPhcs() {
        return phcRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(p -> new PhcDto(p.getCode(), p.getName(), p.getTaluka(), p.getDistrict(),
                        p.getStateCode(), p.getLatitude(), p.getLongitude(), p.isBackupTeam()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Facility requireFacility(String code) {
        return facilityRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FACILITY_NOT_FOUND", "Facility not found"));
    }

    @Transactional(readOnly = true)
    public List<PoolDayDto> poolDays(String facilityCode, int days) {
        Facility facility = requireFacility(facilityCode);
        List<PoolDayDto> out = new ArrayList<>();
        LocalDate d = LocalDate.now();
        int added = 0;
        while (added < days) {
            boolean open = isOpen(facilityCode, d);
            long used = bookingRepository.countByFacilityCodeAndBookingDateAndStatus(
                    facilityCode, d, BookingStatus.ACTIVE);
            int capacity = facility.getDailyPoolCap() > 0 ? facility.getDailyPoolCap() : POOL_CAP;
            int left = open ? Math.max(0, capacity - (int) used) : 0;
            out.add(new PoolDayDto(d, (int) used, capacity, left, open));
            if (open) {
                added++;
            }
            d = d.plusDays(1);
            if (out.size() > 40) {
                break;
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public int nextPlaceNo(String facilityCode, LocalDate date) {
        long used = bookingRepository.countByFacilityCodeAndBookingDateAndStatus(
                facilityCode, date, BookingStatus.ACTIVE);
        Facility facility = requireFacility(facilityCode);
        int capacity = facility.getDailyPoolCap() > 0 ? facility.getDailyPoolCap() : POOL_CAP;
        if (!isOpen(facilityCode, date) || used >= capacity) {
            throw new ApiException(HttpStatus.CONFLICT, "POOL_FULL",
                    "No green-channel places left for this date");
        }
        return (int) used + 1;
    }

    public boolean isOpen(String facilityCode, LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SUNDAY) {
            return false;
        }
        if ("CHCK".equals(facilityCode)) {
            return dow == DayOfWeek.TUESDAY || dow == DayOfWeek.FRIDAY;
        }
        if ("RCC".equals(facilityCode)) {
            return dow != DayOfWeek.SATURDAY;
        }
        return true;
    }

    private FacilityDto toDto(Facility f) {
        List<String> services = Arrays.stream(f.getServicesCsv().split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
        return new FacilityDto(
                f.getCode(), f.getName(), f.getShortName(), f.getFacilityType(),
                f.getAddressHint(), f.getHours(), f.getLatitude(), f.getLongitude(),
                f.getDailyPoolCap(), f.getReportDays(), services);
    }
}
