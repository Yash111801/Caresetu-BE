package in.gov.npncd.caresetu.mapper;

import in.gov.npncd.caresetu.domain.entity.Permission;
import in.gov.npncd.caresetu.domain.entity.Role;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.dto.auth.UserProfileDto;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileDto toProfile(UserAccount user) {
        return new UserProfileDto(
                user.getPublicId(),
                user.getFullName(),
                maskMobile(user.getMobile()),
                user.getFacilityCode(),
                user.getPhcCode(),
                user.getTaluka(),
                user.getDistrict(),
                user.getStateCode(),
                user.getRoles().stream().map(Role::getCode).collect(Collectors.toUnmodifiableSet()),
                user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getCode)
                        .collect(Collectors.toUnmodifiableSet()));
    }

    public UserProfileDto toProfile(UserPrincipal principal, UserAccount user) {
        return new UserProfileDto(
                principal.getPublicId(),
                principal.getFullName(),
                maskMobile(principal.getUsername()),
                user.getFacilityCode(),
                user.getPhcCode(),
                user.getTaluka(),
                user.getDistrict(),
                user.getStateCode(),
                principal.getRoles(),
                principal.getPermissions());
    }

    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "****";
        }
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
