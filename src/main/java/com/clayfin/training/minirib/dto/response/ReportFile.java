package com.clayfin.training.minirib.dto.response;

public record ReportFile(byte[] content, String fileName, String contentType) {
}