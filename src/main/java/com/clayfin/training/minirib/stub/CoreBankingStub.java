package com.clayfin.training.minirib.stub;

import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CoreBankingStub {

    private final ProducerTemplate producerTemplate;

    public List<CoreAccount> getAccountsByCif(String cif) {
        CoreAccount[] accounts = producerTemplate.requestBodyAndHeader(
                "direct:getAccountsByCif", null, "cif", cif, CoreAccount[].class);

        if (accounts == null) {
            return List.of();
        }
        return Arrays.stream(accounts)
                .sorted(Comparator.comparing(CoreAccount::accNumber))
                .toList();
    }
}