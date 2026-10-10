package com.clayfin.training.minirib.controller;

import com.clayfin.training.minirib.dto.request.PaymentReportRequest;
import com.clayfin.training.minirib.dto.request.PaymentRequest;
import com.clayfin.training.minirib.dto.response.PaymentResponse;
import com.clayfin.training.minirib.dto.response.ReportFile;
import com.clayfin.training.minirib.service.PaymentReportService;
import com.clayfin.training.minirib.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReportService paymentReportService;

    @PostMapping
    public ResponseEntity<PaymentResponse> makePayment(@AuthenticationPrincipal String cif,
                                                       @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.makePayment(cif, request));
    }

    @PostMapping("/report")
    public ResponseEntity<byte[]> downloadReport(@AuthenticationPrincipal String cif,
                                                 @Valid @RequestBody PaymentReportRequest request) {
        ReportFile file = paymentReportService.generateReport(cif, request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.fileName())
                .body(file.content());
    }
}
