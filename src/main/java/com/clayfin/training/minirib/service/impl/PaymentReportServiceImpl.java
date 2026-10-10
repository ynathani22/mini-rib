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
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReportServiceImpl implements PaymentReportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String[] HEADERS =
            {"Payment ID", "Payment Date", "Value Date", "Transfer Type", "Debit Account", "Credit Account", "Amount", "Remarks"};

    private final AppUserRepository appUserRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final EmailService emailService;

    private JasperReport compiledReport;

    @Override
    @Transactional(readOnly = true)
    public ReportFile generateReport(String cif, PaymentReportRequest request) {
        AppUser user = appUserRepository.findByCif(cif)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        // ulp_id, if sent, must be the logged-in user
        if (request.ulpId() != null && !request.ulpId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        List<PaymentDetail> payments = paymentDetailRepository
                .findByCreatedByAndTransferTypeAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(
                        user.getId(), request.transferType(), request.fromDate(), request.toDate());

        BigDecimal total = payments.stream().map(PaymentDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String baseName = "payments-" + request.transferType() + "-" + request.fromDate() + "-to-" + request.toDate();

        ReportFile file = switch (request.downloadType()) {
            case PDF -> new ReportFile(buildPdf(user, request, payments, total), baseName + ".pdf", "application/pdf");
            case XLS -> new ReportFile(buildXls(payments, total), baseName + ".xls", "application/vnd.ms-excel");
        };

        emailService.sendWithAttachment(
                user.getEmail(),
                "Payment details " + request.transferType() + " " + request.fromDate() + " to " + request.toDate(),
                "Dear " + user.getFullName() + ",\n\nPlease find attached your payment details ("
                        + payments.size() + " payments).\n\nMini RIB",
                file);

        log.info("Payment report {} generated for userId={}, rows={}", request.downloadType(), user.getId(), payments.size());
        return file;
    }

    // ---------- PDF (JasperReports 7 + OpenPDF) ----------
    private byte[] buildPdf(AppUser user, PaymentReportRequest request, List<PaymentDetail> payments, BigDecimal total) {
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

        Map<String, Object> params = new HashMap<>();
        params.put("customerName", user.getFullName());
        params.put("transferType", request.transferType().name());
        params.put("fromDate", request.fromDate().format(DATE));
        params.put("toDate", request.toDate().format(DATE));
        params.put("totalAmount", String.format("%,.2f", total));
        params.put("generatedOn", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

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

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PaymentDetail p : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getPaymentDate().format(DATE));
                row.createCell(2).setCellValue(p.getValueDate().format(DATE));
                row.createCell(3).setCellValue(p.getTransferType().name());
                row.createCell(4).setCellValue(p.getDebitAccount());     // text, so 12 digits don't become 1.0E11
                row.createCell(5).setCellValue(p.getCreditAccount());
                Cell amount = row.createCell(6);
                amount.setCellValue(p.getAmount().doubleValue());
                amount.setCellStyle(amountStyle);
                row.createCell(7).setCellValue(p.getRemarks() == null ? "" : p.getRemarks());
            }

            Row totalRow = sheet.createRow(rowNum);
            Cell label = totalRow.createCell(5);
            label.setCellValue("Total");
            label.setCellStyle(headerStyle);
            Cell totalCell = totalRow.createCell(6);
            totalCell.setCellValue(total.doubleValue());
            totalCell.setCellStyle(amountStyle);

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