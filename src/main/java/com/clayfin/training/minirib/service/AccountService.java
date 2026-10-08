package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.response.AccountSummaryResponse;

public interface AccountService {

    AccountSummaryResponse getAccountSummary(String cif);
}