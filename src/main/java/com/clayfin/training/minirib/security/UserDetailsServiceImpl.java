package com.clayfin.training.minirib.security;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.enums.UserStatus;
import com.clayfin.training.minirib.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        AppUser user = appUserRepository.findByLoginId(loginId.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getLoginId())
                .password(user.getPasswordHash())
                .accountLocked(user.getStatus() == UserStatus.LOCKED)
                .disabled(user.getStatus() == UserStatus.INACTIVE)
                .roles("CUSTOMER")
                .build();
    }
}
