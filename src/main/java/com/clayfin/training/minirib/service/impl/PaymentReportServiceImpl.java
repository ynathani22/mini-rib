package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.domain.PaymentDetail;
import com.clayfin.training.minirib.dto.request.PaymentReportRequest;
import com.clayfin.training.minirib.dto.response.ReportFile;
import com.clayfin.training.minirib.exception.BusinessException;
import com.clayfin.training.minirib.exception.ErrorCode;
import com.clayfin.training.minirib.repository.AppUserRepository;
import com.clayfin.training.minirib.repository.PaymentDetailRepository;
import com.clayfin.training.minirib.service.EmailService;
import com.clayfin.training.minirib.service.PaymentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReportServiceImpl implements PaymentReportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String EMAIL_TEMPLATE = "email/payment-report";
    private static final String[] HEADERS =
            {"Payment ID", "Payment Date", "Value Date", "Transfer Type", "Debit Account", "Credit Account", "Amount", "Remarks"};

    private final AppUserRepository appUserRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final EmailService emailService;

    private JasperReport compiledReport;   // compiled once, reused for every request

    @Override
    @Transactional(readOnly = true)
    public ReportFile generateReport(String cif, PaymentReportRequest request) {
        // 1. logged-in user (from the token's CIF)
        AppUser user = appUserRepository.findByCif(cif)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        // 2. ulp_id, if sent, must be the logged-in user
        if (request.ulpId() != null && !request.ulpId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. only this user's payments, for the given type and date range
        List<PaymentDetail> payments = paymentDetailRepository
                .findByCreatedByAndTransferTypeAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(
                        user.getId(), request.transferType(), request.fromDate(), request.toDate());

        BigDecimal total = payments.stream()
                .map(PaymentDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. build the file
        String baseName = "payments-" + request.transferType() + "-" + request.fromDate() + "-to-" + request.toDate();
        ReportFile file = switch (request.downloadType()) {
            case PDF -> new ReportFile(buildPdf(user, request, payments, total),
                    baseName + ".pdf", "application/pdf");
            case XLS -> new ReportFile(buildXls(payments, total),
                    baseName + ".xls", "application/vnd.ms-excel");
        };

        // 5. email it to the user's registered address using the HTML template
        emailService.sendTemplateWithAttachment(
                user.getEmail(),
                "Your " + request.transferType() + " payment details",
                EMAIL_TEMPLATE,
                buildMailVariables(user, request, payments.size(), total),
                file);

        log.info("Payment report {} generated for userId={}, rows={}",
                request.downloadType(), user.getId(), payments.size());
        return file;
    }

    // ---------- Email template variables ----------
    private Map<String, Object> buildMailVariables(AppUser user, PaymentReportRequest request,
                                                   int paymentCount, BigDecimal total) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", user.getFullName());
        vars.put("transferType", request.transferType().name());
        vars.put("fromDate", request.fromDate().format(DATE));
        vars.put("toDate", request.toDate().format(DATE));
        vars.put("paymentCount", paymentCount);
        vars.put("totalAmount", String.format("%,.2f", total));
        vars.put("downloadType", request.downloadType().name());
        return vars;
    }

    // ---------- PDF (JasperReports 7 + OpenPDF) ----------
    private byte[] buildPdf(AppUser user, PaymentReportRequest request,
                            List<PaymentDetail> payments, BigDecimal total) {
        // each payment -> one row; keys must match <field name="..."> in the .jrxml
        List<Map<String, ?>> rows = new ArrayList<>();
        for (PaymentDetail p : payments) {
            Map<String, Object> row = new HashMap<>();
            row.put("paymentId", String.valueOf(p.getId()));
            row.put("paymentDate", p.getPaymentDate().format(DATE));
            row.put("valueDate", p.getValueDate().format(DATE));
            row.put("transferType", p.getTransferType().name());
            row.put("debitAccount", p.getDebitAccount());
            row.put("creditAccount", p.getCreditAccount());
            row.put("amount", String.format("%,.2f", p.getAmount()));
            row.put("remarks", p.getRemarks() == null ? "" : p.getRemarks());
            rows.add(row);
        }

        // report-level values -> $P{...} in the .jrxml
        Map<String, Object> params = new HashMap<>();
        params.put("customerName", user.getFullName());
        params.put("transferType", request.transferType().name());
        params.put("fromDate", request.fromDate().format(DATE));
        params.put("toDate", request.toDate().format(DATE));
        params.put("totalAmount", String.format("%,.2f", total));
        params.put("generatedOn", LocalDateTime.now().format(DATE_TIME));

        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(), params, new JRMapCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new BusinessException(ErrorCode.REPORT_GENERATION_FAILED);
        }
    }

    private synchronized JasperReport getReport() throws Exception {
        if (compiledReport == null) {
            try (InputStream in = new ClassPathResource("reports/payment-details.jrxml").getInputStream()) {
                compiledReport = JasperCompileManager.compileReport(in);
            }
        }
        return compiledReport;
    }

    // ---------- XLS (Apache POI) ----------
    private byte[] buildXls(List<PaymentDetail> payments, BigDecimal total) {
        try (Workbook workbook = new HSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payments");

            // styles are created once, outside the loop
            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // data rows
            int rowNum = 1;
            for (PaymentDetail p : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getPaymentDate().format(DATE));
                row.createCell(2).setCellValue(p.getValueDate().format(DATE));
                row.createCell(3).setCellValue(p.getTransferType().name());
                row.createCell(4).setCellValue(p.getDebitAccount());      // text, so 12 digits stay exact
                row.createCell(5).setCellValue(p.getCreditAccount());
                Cell amount = row.createCell(6);
                amount.setCellValue(p.getAmount().doubleValue());
                amount.setCellStyle(amountStyle);
                row.createCell(7).setCellValue(p.getRemarks() == null ? "" : p.getRemarks());
            }

            // total row
            Row totalRow = sheet.createRow(rowNum);
            Cell label = totalRow.createCell(5);
            label.setCellValue("Total");
            label.setCellStyle(headerStyle);
            Cell totalCell = totalRow.createCell(6);
            totalCell.setCellValue(total.doubleValue());
            totalCell.setCellStyle(amountStyle);

            sheet.createFreezePane(0, 1);   // header stays visible while scrolling
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("XLS generation failed", e);
            throw new BusinessException(ErrorCode.REPORT_GENERATION_FAILED);
        }
    }
}