package com.clayfin.training.minirib.dto.response;

import com.clayfin.training.minirib.enums.UserStatus;

import java.time.LocalDateTime;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String cif,
        String loginId,
        String fullName,
        String email,
        String mobileNo,
        UserStatus status,
        LocalDateTime lastLoginAt
) {
}
