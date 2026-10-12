package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.response.ReportFile;

import java.util.Map;

public interface EmailService {

    void sendTemplateWithAttachment(String to, String subject, String templateName,
                                    Map<String, Object> variables, ReportFile attachment);
}