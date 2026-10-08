package com.clayfin.training.minirib.stub;

import com.clayfin.training.minirib.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stub/accounts")
@RequiredArgsConstructor
public class StubController {

    private final CoreBankingStub coreBankingStub;

    @GetMapping("/{cif}")
    public List<CoreAccount> getAccounts(@PathVariable String cif) {
        return coreBankingStub.getAccountsByCif(cif);
    }

    @PostMapping
    public ResponseEntity<CoreAccount> addAccount(@RequestBody CoreAccount account) {
        coreBankingStub.addAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PatchMapping("/{accNumber}/status")
    public CoreAccount updateStatus(@PathVariable String accNumber, @RequestParam AccountStatus status) {
        return coreBankingStub.updateStatus(accNumber, status);
    }
}