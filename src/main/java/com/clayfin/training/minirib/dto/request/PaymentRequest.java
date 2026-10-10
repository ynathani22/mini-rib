package com.clayfin.training.minirib.dto.request;

import com.clayfin.training.minirib.enums.TransferType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(message = "transfer_type is required")
        @JsonProperty("transfer_type")
        TransferType transferType,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        @Digits(integer = 15, fraction = 2, message = "amount can have at most 2 decimal places")
        BigDecimal amount,

        @Size(max = 100, message = "remarks must be at most 100 characters")
        String remarks,

        @NotBlank(message = "debit_account is required")
        @Pattern(regexp = "^\\d{12}$", message = "debit_account must be exactly 12 digits")
        @JsonProperty("debit_account")
        String debitAccount,

        @NotBlank(message = "credit_account is required")
        @Pattern(regexp = "^\\d{12}$", message = "credit_account must be exactly 12 digits")
        @JsonProperty("credit_account")
        String creditAccount
) {
    @AssertTrue(message = "debit_account and credit_account must be different")
    public boolean isDifferentAccounts() {
        return debitAccount == null || !debitAccount.equals(creditAccount);
    }
}