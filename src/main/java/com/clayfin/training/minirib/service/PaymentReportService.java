package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.request.PaymentReportRequest;
import com.clayfin.training.minirib.dto.response.ReportFile;

public interface PaymentReportService {

    ReportFile generateReport(String cif, PaymentReportRequest request);
}