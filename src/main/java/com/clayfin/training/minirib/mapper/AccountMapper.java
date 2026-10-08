package com.clayfin.training.minirib.mapper;

import com.clayfin.training.minirib.domain.CifAccount;
import com.clayfin.training.minirib.dto.response.AccountSummaryResponse;
import com.clayfin.training.minirib.service.impl.AccountDiff;
import com.clayfin.training.minirib.stub.CoreAccount;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    public CifAccount toEntity(CoreAccount core) {
        CifAccount account = new CifAccount();
        account.setCif(core.cif());
        account.setAccNumber(core.accNumber());
        account.setAccHolderName(core.accHolderName());
        account.setCurrCode(core.currCode());
        account.setDtype(core.dtype());
        account.setStatus(core.status());
        return account;
    }

    public AccountSummaryResponse toSummary(String cif, List<CifAccount> accounts, AccountDiff diff) {
        List<AccountSummaryResponse.AccountItem> items = accounts.stream()
                .map(a -> new AccountSummaryResponse.AccountItem(a.getAccNumber(), a.getAccHolderName(),
                        a.getCurrCode(), a.getDtype(), a.getStatus(), a.getVersion()))
                .toList();

        return new AccountSummaryResponse(cif, items.size(),
                diff.hasChanges() ? "UPDATED_FROM_CORE" : "NO_CHANGE",
                diff.toInsert().size(), diff.toUpdate().size(), items);
    }
}