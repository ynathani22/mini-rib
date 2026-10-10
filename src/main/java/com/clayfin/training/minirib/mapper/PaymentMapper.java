package com.clayfin.training.minirib.mapper;

import com.clayfin.training.minirib.domain.PaymentDetail;
import com.clayfin.training.minirib.dto.request.PaymentRequest;
import com.clayfin.training.minirib.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDetail toEntity(PaymentRequest request, Long userId) {
        PaymentDetail payment = new PaymentDetail();
        payment.setTransferType(request.transferType());
        payment.setAmount(request.amount());
        payment.setRemarks(request.remarks());
        payment.setDebitAccount(request.debitAccount());
        payment.setCreditAccount(request.creditAccount());
        payment.setCreatedBy(userId);
        return payment;
    }

    public PaymentResponse toResponse(PaymentDetail p) {
        return new PaymentResponse(p.getId(), p.getPaymentDate(), p.getValueDate(), p.getTransferType(),
                p.getDebitAccount(), p.getCreditAccount(), p.getAmount(), p.getRemarks(), p.getCreatedDate());
    }
}