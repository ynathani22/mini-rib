package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.dto.request.LoginRequest;
import com.clayfin.training.minirib.dto.request.RegisterRequest;
import com.clayfin.training.minirib.dto.response.LoginResponse;
import com.clayfin.training.minirib.dto.response.RegisterResponse;
import com.clayfin.training.minirib.enums.UserStatus;
import com.clayfin.training.minirib.exception.BusinessException;
import com.clayfin.training.minirib.exception.ErrorCode;
import com.clayfin.training.minirib.mapper.UserMapper;
import com.clayfin.training.minirib.repository.AppUserRepository;
import com.clayfin.training.minirib.security.JwtService;
import com.clayfin.training.minirib.service.AuthService;
import com.clayfin.training.minirib.stub.CoreAccount;
import com.clayfin.training.minirib.stub.CoreBankingStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;
    private final CoreBankingStub coreBankingStub;

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
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String cif = request.cif().trim().toUpperCase();
        String loginId = request.loginId().trim().toLowerCase();

        // the CIF must have at least one account in core banking (stub)
        List<CoreAccount> accounts = coreBankingStub.getAccountsByCif(cif);
        if (accounts.isEmpty()) {
            throw new BusinessException(ErrorCode.CIF_NOT_FOUND);
        }
        if (appUserRepository.existsByCif(cif)) {
            throw new BusinessException(ErrorCode.CIF_ALREADY_REGISTERED);
        }
        if (appUserRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.LOGIN_ID_TAKEN);
        }

        AppUser user = new AppUser();
        user.setCif(cif);
        user.setLoginId(loginId);
        user.setPasswordHash(passwordEncoder.encode(request.password()));   // BCrypt one-way hash
        user.setFullName(accounts.get(0).accHolderName());
        user.setEmail(request.email());
        user.setMobileNo(request.mobileNo());

        AppUser saved = appUserRepository.save(user);
        log.info("User registered, loginId={}", loginId);
        return userMapper.toRegisterResponse(saved);
    }
}
