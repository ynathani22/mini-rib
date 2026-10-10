package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.request.PaymentRequest;
import com.clayfin.training.minirib.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse makePayment(String cif, PaymentRequest request);
}
