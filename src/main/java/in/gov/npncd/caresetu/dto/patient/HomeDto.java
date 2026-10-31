package in.gov.npncd.caresetu.dto.patient;

import java.util.List;
import java.util.Map;

public record HomeDto(
        String greeting,
        String roleTitle,
        Map<String, Long> stats,
        List<PatientSummaryDto> worklist,
        List<String> tips
) {}
