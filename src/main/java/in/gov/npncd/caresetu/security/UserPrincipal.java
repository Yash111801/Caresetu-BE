package in.gov.npncd.caresetu.security;

import in.gov.npncd.caresetu.domain.entity.UserAccount;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String publicId;
    private final String mobile;
    private final String fullName;
    private final boolean active;
    private final boolean locked;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UserAccount user) {
        this.id = user.getId();
        this.publicId = user.getPublicId();
        this.mobile = user.getMobile();
        this.fullName = user.getFullName();
        this.active = user.isActive();
        this.locked = user.isLocked();
        this.roles = user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toUnmodifiableSet());
        this.permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toUnmodifiableSet());
        this.authorities = Stream.concat(
                        roles.stream().map(code -> "ROLE_" + code),
                        permissions.stream().map(code -> "PERM_" + code))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getFullName() {
        return fullName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return mobile;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
