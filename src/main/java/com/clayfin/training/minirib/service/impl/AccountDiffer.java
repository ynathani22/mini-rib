package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.CifAccount;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AccountDiffer {

    // two accounts are "the same" when every field below is equal (compare returns 0)
    static final Comparator<CifAccount> ACCOUNT_COMPARATOR = Comparator
            .comparing(CifAccount::getAccNumber)
            .thenComparing(CifAccount::getAccHolderName)
            .thenComparing(CifAccount::getCurrCode)
            .thenComparing(CifAccount::getDtype)
            .thenComparing(CifAccount::getStatus);

    public AccountDiff diff(List<CifAccount> fromApi, List<CifAccount> fromDb) {
        Map<String, CifAccount> dbByAccNumber = fromDb.stream()
                .collect(Collectors.toMap(CifAccount::getAccNumber, Function.identity()));

        List<CifAccount> toInsert = new ArrayList<>();
        List<CifAccount> toUpdate = new ArrayList<>();

        for (CifAccount api : fromApi) {
            CifAccount db = dbByAccNumber.get(api.getAccNumber());

            if (db == null) {
                toInsert.add(api);                                    // new in API -> insert
            } else if (ACCOUNT_COMPARATOR.compare(api, db) != 0) {
                db.setAccHolderName(api.getAccHolderName());          // changed -> copy new values
                db.setCurrCode(api.getCurrCode());
                db.setDtype(api.getDtype());
                db.setStatus(api.getStatus());
                toUpdate.add(db);
            }
            // compare == 0 -> no difference, nothing to do
        }
        return new AccountDiff(toInsert, toUpdate);
    }
}