package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.response.ReportFile;

public interface EmailService {

    void sendWithAttachment(String to, String subject, String body, ReportFile attachment);
}