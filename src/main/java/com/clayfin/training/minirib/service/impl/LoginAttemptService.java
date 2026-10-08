package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.enums.UserStatus;
import com.clayfin.training.minirib.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final AppUserRepository appUserRepository;

    // separate transaction, so the update is saved even though login() throws afterwards
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        user.setFailedLoginCount(user.getFailedLoginCount() + 1);
        user.setLastLoginFailureAt(LocalDateTime.now());

        if (user.getFailedLoginCount() >= MAX_FAILED_ATTEMPTS && user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.LOCKED);
            log.warn("User locked after {} failed logins, loginId={}", user.getFailedLoginCount(), user.getLoginId());
        }
    }
}
