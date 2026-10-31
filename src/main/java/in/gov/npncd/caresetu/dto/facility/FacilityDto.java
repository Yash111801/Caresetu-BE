package in.gov.npncd.caresetu.dto.facility;

import java.math.BigDecimal;
import java.util.List;

public record FacilityDto(
        String code, String name, String shortName, String facilityType,
        String addressHint, String hours, BigDecimal latitude, BigDecimal longitude,
        int dailyPoolCap, int reportDays, List<String> services
) {}
