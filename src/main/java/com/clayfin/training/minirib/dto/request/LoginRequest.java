package com.clayfin.training.minirib.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "karim.ahmed")
        @NotBlank(message = "loginId is required")
        String loginId,

        @Schema(example = "Str0ng@Pass")
        @NotBlank(message = "password is required")
        String password
) {
    // keep the password out of logs
    @Override
    public String toString() {
        return "LoginRequest[loginId=" + loginId + "]";
    }
}
