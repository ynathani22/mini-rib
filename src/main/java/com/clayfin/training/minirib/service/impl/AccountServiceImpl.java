package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.CifAccount;
import com.clayfin.training.minirib.dto.response.AccountSummaryResponse;
import com.clayfin.training.minirib.mapper.AccountMapper;
import com.clayfin.training.minirib.repository.CifAccountRepository;
import com.clayfin.training.minirib.service.AccountService;
import com.clayfin.training.minirib.stub.CoreBankingStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CoreBankingStub coreBankingStub;
    private final CifAccountRepository cifAccountRepository;
    private final AccountDiffer accountDiffer;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountSummaryResponse getAccountSummary(String cif) {
        // 1. accounts from the API (stub)
        List<CifAccount> fromApi = coreBankingStub.getAccountsByCif(cif).stream()
                .map(accountMapper::toEntity)
                .toList();

        // 2. accounts already in our DB
        List<CifAccount> fromDb = cifAccountRepository.findByCifOrderByAccNumberAsc(cif);

        // 3. compare
        AccountDiff diff = accountDiffer.diff(fromApi, fromDb);

        // 4. no difference -> return DB values as they are
        if (!diff.hasChanges()) {
            log.info("Account summary for cif={}: no change", cif);
            return accountMapper.toSummary(cif, fromDb, diff);
        }

        // 5. difference -> save it, then return the fresh DB values
        cifAccountRepository.saveAll(diff.toInsert());
        cifAccountRepository.saveAll(diff.toUpdate());
        cifAccountRepository.flush();
        log.info("Account summary for cif={}: inserted={}, updated={}",
                cif, diff.toInsert().size(), diff.toUpdate().size());

        return accountMapper.toSummary(cif, cifAccountRepository.findByCifOrderByAccNumberAsc(cif), diff);
    }
}
