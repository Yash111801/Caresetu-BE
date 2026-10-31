package in.gov.npncd.caresetu.domain.entity;

import in.gov.npncd.caresetu.domain.enums.BookingStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    @Column(name = "facility_code", nullable = false, length = 16)
    private String facilityCode;
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    @Column(name = "window_label", nullable = false, length = 40)
    private String windowLabel;
    @Column(name = "place_no", nullable = false)
    private int placeNo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.ACTIVE;
    @Column(name = "booked_by_user_id")
    private Long bookedByUserId;
    @Column(name = "booked_by_name", length = 150)
    private String bookedByName;
    @Column(name = "booking_kind", nullable = false, length = 20)
    private String bookingKind = "HOSPITAL";
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
