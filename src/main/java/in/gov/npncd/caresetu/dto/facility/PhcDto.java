package in.gov.npncd.caresetu.dto.facility;

import java.math.BigDecimal;

public record PhcDto(
        String code, String name, String taluka, String district, String stateCode,
        BigDecimal latitude, BigDecimal longitude, boolean backupTeam
) {}
