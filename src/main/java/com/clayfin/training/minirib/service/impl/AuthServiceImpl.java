package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.dto.request.LoginRequest;
import com.clayfin.training.minirib.dto.response.LoginResponse;
import com.clayfin.training.minirib.enums.UserStatus;
import com.clayfin.training.minirib.exception.BusinessException;
import com.clayfin.training.minirib.exception.ErrorCode;
import com.clayfin.training.minirib.mapper.UserMapper;
import com.clayfin.training.minirib.repository.AppUserRepository;
import com.clayfin.training.minirib.security.JwtService;
import com.clayfin.training.minirib.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String loginId = request.loginId().trim().toLowerCase();

        AppUser user = appUserRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.info("Login failed, unknown loginId={}", loginId);
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.info("Login failed, wrong password for loginId={}", loginId);
            loginAttemptService.recordFailure(user.getId());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        LocalDateTime previousLogin = user.getLastLoginSuccessAt();
        user.setLastLoginSuccessAt(LocalDateTime.now());
        user.setFailedLoginCount(0);

        String token = jwtService.generateToken(user);
        log.info("Login success for loginId={}", loginId);

        return userMapper.toLoginResponse(user, token, jwtService.getExpirationSeconds(), previousLogin);
    }
}
