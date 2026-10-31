package in.gov.npncd.caresetu.dto.patient;

import java.time.LocalDate;
import java.util.List;

public record PatientSummaryDto(
        String id, String uhid, String fullName, int age, String sex, String mobile,
        String village, String phcCode, String cancerType, String presentation, String stage,
        String riskLevel, String assignedFacCode, LocalDate visitAt, String visitWindow,
        List<WorkActionDto> actions
) {}
