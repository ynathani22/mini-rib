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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private LoginAttemptService loginAttemptService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private AuthServiceImpl authService;
    private AppUser user;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(appUserRepository, passwordEncoder, jwtService,
                loginAttemptService, new UserMapper());

        user = new AppUser();
        user.setId(1L);
        user.setCif("CIF0001");
        user.setLoginId("karim.ahmed");
        user.setPasswordHash(passwordEncoder.encode("Str0ng@Pass"));
        user.setFullName("Karim Ahmed");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginCount(2);
    }

    @Test
    void loginSuccess() {
        LocalDateTime previous = LocalDateTime.of(2026, 10, 5, 18, 42);
        user.setLastLoginSuccessAt(previous);
        when(appUserRepository.findByLoginId("karim.ahmed")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.getExpirationSeconds()).thenReturn(900L);

        LoginResponse response = authService.login(new LoginRequest("Karim.Ahmed", "Str0ng@Pass"));

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.cif()).isEqualTo("CIF0001");
        assertThat(response.lastLoginAt()).isEqualTo(previous);
        assertThat(user.getFailedLoginCount()).isZero();
        verify(loginAttemptService, never()).recordFailure(anyLong());
    }

    @Test
    void unknownUser() {
        when(appUserRepository.findByLoginId("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody", "Str0ng@Pass")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void wrongPassword() {
        when(appUserRepository.findByLoginId("karim.ahmed")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("karim.ahmed", "Wrong@123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(loginAttemptService).recordFailure(1L);
    }

    @Test
    void lockedUser() {
        user.setStatus(UserStatus.LOCKED);
        when(appUserRepository.findByLoginId("karim.ahmed")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("karim.ahmed", "Str0ng@Pass")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_LOCKED);
    }
}
