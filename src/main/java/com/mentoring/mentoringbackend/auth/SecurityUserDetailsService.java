package com.mentoring.mentoringbackend.auth;

import com.mentoring.mentoringbackend.user.domain.User;
import com.mentoring.mentoringbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Inactive user: " + email);
        }

        return SecurityUserDetails.from(user);
    }
}
