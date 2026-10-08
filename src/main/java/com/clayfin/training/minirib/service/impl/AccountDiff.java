package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.CifAccount;

import java.util.List;

public record AccountDiff(List<CifAccount> toInsert, List<CifAccount> toUpdate) {

    public boolean hasChanges() {
        return !toInsert.isEmpty() || !toUpdate.isEmpty();
    }
}