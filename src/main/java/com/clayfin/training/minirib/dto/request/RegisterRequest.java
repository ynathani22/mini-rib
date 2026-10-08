package com.clayfin.training.minirib.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(

        @NotBlank(message = "cif is required")
        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "cif must be 4 to 20 letters or digits")
        String cif,

        @NotBlank(message = "loginId is required")
        @Pattern(regexp = "^[A-Za-z0-9._]{6,30}$",
                message = "loginId must be 6 to 30 characters: letters, digits, . or _")
        String loginId,

        @NotBlank(message = "password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$",
                message = "password must be 8 to 20 characters with upper case, lower case, digit and special character")
        String password,

        @NotBlank(message = "confirmPassword is required")
        String confirmPassword,

        @Email(message = "email is not valid")
        String email,

        @Pattern(regexp = "^[0-9]{10,15}$", message = "mobileNo must be 10 to 15 digits")
        String mobileNo
) {
    @AssertTrue(message = "password and confirmPassword must match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "RegisterRequest[cif=" + cif + ", loginId=" + loginId + "]";
    }
}