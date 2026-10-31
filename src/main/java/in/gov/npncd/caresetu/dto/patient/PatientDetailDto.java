package in.gov.npncd.caresetu.dto.patient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PatientDetailDto(
        String id, String uhid, String fullName, int age, String sex, String mobile,
        String abhaId, String smsLang, String village, String phcCode, String taluka,
        String district, String stateCode, String cancerType, String presentation, String stage,
        String riskLevel, String screenedByName, String screenedByRole, Instant screenedAt,
        String findingNotes, String assignedFacCode, LocalDate visitAt, String visitWindow,
        String testFacCode, LocalDate reportDue, Instant reportAt, String testResult,
        LocalDate oncoVisitAt, String treatmentPlan, String closedReason, boolean moAcked,
        boolean counselled, LocalDate monitorUntil, BigDecimal latitude, BigDecimal longitude,
        String incomeBand, String rationCard, Boolean hasPmjayCard, boolean esic,
        boolean govtEmployee, boolean privateInsurance, boolean gpsConsent, boolean dataConsent,
        String whoClass, String entrySource, boolean treatmentStarted, String exitReason,
        Instant informedAt, Instant assessedAt, Instant consultAt,
        List<WorkActionDto> actions, List<PatientEventDto> timeline,
        List<BookingDto> bookings, List<DiagnosticTestDto> tests,
        Map<String, Object> eligibility,
        Map<String, Object> miss,
        List<Map<String, Object>> contacts
) {
}
