package com.clayfin.training.minirib.dto.response;

import com.clayfin.training.minirib.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        LocalDate paymentDate,
        LocalDate valueDate,
        TransferType transferType,
        String debitAccount,
        String creditAccount,
        BigDecimal amount,
        String remarks,
        LocalDateTime createdDate
) {
}