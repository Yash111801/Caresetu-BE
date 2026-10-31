package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.Booking;
import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByFacilityCodeAndBookingDateAndStatus(String facilityCode, LocalDate date, BookingStatus status);
    long countByFacilityCodeAndBookingDateAndStatus(String facilityCode, LocalDate date, BookingStatus status);
    List<Booking> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<Booking> findFirstByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, BookingStatus status);
    List<Booking> findByFacilityCodeAndBookingDateBetweenAndStatusOrderByBookingDateAscPlaceNoAsc(
            String facilityCode, LocalDate from, LocalDate to, BookingStatus status);
}
