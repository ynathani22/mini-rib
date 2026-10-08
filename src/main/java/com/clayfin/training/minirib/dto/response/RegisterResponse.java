package com.clayfin.training.minirib.dto.response;

import com.clayfin.training.minirib.enums.UserStatus;

import java.time.LocalDateTime;

public record RegisterResponse(Long userId, String cif, String loginId, String fullName,
                               UserStatus status, LocalDateTime createdAt) {
}