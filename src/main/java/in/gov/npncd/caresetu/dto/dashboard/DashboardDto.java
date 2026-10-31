package in.gov.npncd.caresetu.dto.dashboard;

import java.util.List;
import java.util.Map;

public record DashboardDto(
        String level,
        String title,
        Map<String, Long> counts,
        List<KpiDto> kpis,
        List<Map<String, Object>> highlights
) {}
