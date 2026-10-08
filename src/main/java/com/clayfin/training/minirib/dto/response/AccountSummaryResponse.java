package com.clayfin.training.minirib.dto.response;

import com.clayfin.training.minirib.enums.AccountDtype;
import com.clayfin.training.minirib.enums.AccountStatus;

import java.util.List;

public record AccountSummaryResponse(
        String cif,
        int totalAccounts,
        String syncResult,
        int inserted,
        int updated,
        List<AccountItem> accounts
) {
    public record AccountItem(
            String accNumber,
            String accHolderName,
            String currCode,
            AccountDtype dtype,
            AccountStatus status,
            Long version
    ) {
    }
}