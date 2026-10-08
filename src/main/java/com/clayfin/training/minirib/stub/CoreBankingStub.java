package com.clayfin.training.minirib.stub;

import com.clayfin.training.minirib.enums.AccountDtype;
import com.clayfin.training.minirib.enums.AccountStatus;
import com.clayfin.training.minirib.exception.BusinessException;
import com.clayfin.training.minirib.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CoreBankingStub {

    // key = account number
    private final Map<String, CoreAccount> accounts = new ConcurrentHashMap<>();

    public CoreBankingStub() {
        addAccount(new CoreAccount("CIF0001", "1001000001", "Yashika Nathani", "BDT", AccountDtype.CASA, AccountStatus.ACTIVE));
        addAccount(new CoreAccount("CIF0001", "1001000002", "Yashika Nathani", "USD", AccountDtype.DEPOSIT, AccountStatus.ACTIVE));
        addAccount(new CoreAccount("CIF0002", "1002000001", "Ritika Kanwar", "BDT", AccountDtype.CASA, AccountStatus.ACTIVE));
        addAccount(new CoreAccount("CIF0002", "1002000002", "Ritika Kanwar", "BDT", AccountDtype.LOAN, AccountStatus.ACTIVE));
        addAccount(new CoreAccount("CIF0003", "1003000001", "Dhriti Sharma", "BDT", AccountDtype.CASA, AccountStatus.ACTIVE));
    }

    public List<CoreAccount> getAccountsByCif(String cif) {
        return accounts.values().stream()
                .filter(a -> a.cif().equals(cif))
                .sorted(Comparator.comparing(CoreAccount::accNumber))
                .toList();
    }

    public void addAccount(CoreAccount account) {
        accounts.put(account.accNumber(), account);
    }

    public CoreAccount updateStatus(String accNumber, AccountStatus status) {
        CoreAccount old = accounts.get(accNumber);
        if (old == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        CoreAccount updated = new CoreAccount(old.cif(), old.accNumber(), old.accHolderName(),
                old.currCode(), old.dtype(), status);
        accounts.put(accNumber, updated);
        return updated;
    }
}