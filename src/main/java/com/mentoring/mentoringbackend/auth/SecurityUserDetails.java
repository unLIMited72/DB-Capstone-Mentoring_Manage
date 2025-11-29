package com.mentoring.mentoringbackend.auth;

import com.mentoring.mentoringbackend.user.domain.Role;
import com.mentoring.mentoringbackend.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class SecurityUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    private SecurityUserDetails(
            Long id,
            String email,
            String password,
            Role role,
            boolean active
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
        this.authorities = createAuthorities(role);
    }

    /** User 엔티티 → SecurityUserDetails 변환 */
    public static SecurityUserDetails from(User user) {
        return new SecurityUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),   // User 엔티티에 passwordHash 필드 만들 예정
                user.getRole(),
                Boolean.TRUE.equals(user.getIsActive())
        );
    }

    private List<GrantedAuthority> createAuthorities(Role role) {
        if (role == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        Stream<String> roles;

        switch (role) {
            case ADMIN -> roles = Stream.of("ROLE_ADMIN");
            case MENTOR -> roles = Stream.of("ROLE_MENTOR");
            case MENTEE -> roles = Stream.of("ROLE_MENTEE");
            case BOTH -> roles = Stream.of("ROLE_MENTOR", "ROLE_MENTEE");
            default -> roles = Stream.of("ROLE_USER");
        }

        return roles
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // username = email
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
