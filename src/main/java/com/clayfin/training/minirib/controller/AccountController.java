package com.clayfin.training.minirib.controller;

import com.clayfin.training.minirib.dto.response.AccountSummaryResponse;
import com.clayfin.training.minirib.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryResponse> getSummary(@AuthenticationPrincipal String cif) {
        return ResponseEntity.ok(accountService.getAccountSummary(cif));
    }
}