package in.gov.npncd.caresetu.dto.patient;

import java.time.LocalDate;

public record BookingDto(
        String id, String facilityCode, LocalDate bookingDate, String windowLabel,
        int placeNo, String status, String bookingKind, String bookedByName
) {}
