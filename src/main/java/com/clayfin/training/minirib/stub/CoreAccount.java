package com.clayfin.training.minirib.stub;

import com.clayfin.training.minirib.enums.AccountDtype;
import com.clayfin.training.minirib.enums.AccountStatus;

public record CoreAccount(
        String cif,
        String accNumber,
        String accHolderName,
        String currCode,
        AccountDtype dtype,
        AccountStatus status
) {
}