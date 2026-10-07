package com.clayfin.training.minirib.mapper;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.dto.response.LoginResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public LoginResponse toLoginResponse(AppUser user, String token, long expiresIn, LocalDateTime lastLoginAt) {
        return new LoginResponse(
                token,
                "Bearer",
                expiresIn,
                user.getId(),
                user.getCif(),
                user.getLoginId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNo(),
                user.getStatus(),
                lastLoginAt);
    }
}
