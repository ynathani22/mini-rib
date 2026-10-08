package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.dto.response.AccountSummaryResponse;
import com.clayfin.training.minirib.service.AccountReportService;
import com.clayfin.training.minirib.service.AccountService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountReportServiceImpl implements AccountReportService {

    private final AccountService accountService;
    private JasperReport compiledReport;   // compiled once, reused

    @Override
    public byte[] generateSummaryPdf(String cif) {
        AccountSummaryResponse summary = accountService.getAccountSummary(cif);

        // each account -> one row of the table
        List<Map<String, ?>> rows = summary.accounts().stream()
                .<Map<String, ?>>map(a -> Map.of(
                        "accNumber", a.accNumber(),
                        "accHolderName", a.accHolderName(),
                        "currCode", a.currCode(),
                        "dtype", a.dtype().name(),
                        "status", a.status().name()))
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("cif", cif);
        params.put("customerName", summary.accounts().isEmpty() ? "-" : summary.accounts().get(0).accHolderName());
        params.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(), params, new JRMapCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate account summary PDF", e);
        }
    }

    private synchronized JasperReport getReport() throws Exception {
        if (compiledReport == null) {
            try (InputStream in = new ClassPathResource("reports/account-summary.jrxml").getInputStream()) {
                compiledReport = JasperCompileManager.compileReport(in);
            }
        }
        return compiledReport;
    }
}