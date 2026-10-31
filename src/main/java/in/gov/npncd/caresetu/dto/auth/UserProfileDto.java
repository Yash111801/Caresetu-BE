package in.gov.npncd.caresetu.dto.auth;

import java.util.Set;

public record UserProfileDto(
        String id,
        String fullName,
        String mobile,
        String facilityCode,
        String phcCode,
        String taluka,
        String district,
        String stateCode,
        Set<String> roles,
        Set<String> permissions
) {
}
