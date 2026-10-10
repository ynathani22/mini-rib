package com.clayfin.training.minirib.service.impl;

import com.clayfin.training.minirib.domain.AppUser;
import com.clayfin.training.minirib.domain.PaymentDetail;
import com.clayfin.training.minirib.dto.request.PaymentRequest;
import com.clayfin.training.minirib.dto.response.PaymentResponse;
import com.clayfin.training.minirib.enums.AccountStatus;
import com.clayfin.training.minirib.enums.TransferType;
import com.clayfin.training.minirib.exception.BusinessException;
import com.clayfin.training.minirib.exception.ErrorCode;
import com.clayfin.training.minirib.mapper.PaymentMapper;
import com.clayfin.training.minirib.repository.AppUserRepository;
import com.clayfin.training.minirib.repository.CifAccountRepository;
import com.clayfin.training.minirib.repository.PaymentDetailRepository;
import com.clayfin.training.minirib.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final AppUserRepository appUserRepository;
    private final CifAccountRepository cifAccountRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse makePayment(String cif, PaymentRequest request) {
        AppUser user = appUserRepository.findByCif(cif)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        // debit account must be the user's own ACTIVE account
        if (!cifAccountRepository.existsByCifAndAccNumberAndStatus(cif, request.debitAccount(), AccountStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.DEBIT_ACCOUNT_NOT_ALLOWED);
        }

        // OWN transfer: credit account must also be the user's own ACTIVE account
        if (request.transferType() == TransferType.OWN
                && !cifAccountRepository.existsByCifAndAccNumberAndStatus(cif, request.creditAccount(), AccountStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.CREDIT_ACCOUNT_NOT_OWN);
        }

        PaymentDetail payment = paymentMapper.toEntity(request, user.getId());
        LocalDate today = LocalDate.now();
        payment.setPaymentDate(today);
        payment.setValueDate(today);

        PaymentDetail saved = paymentDetailRepository.save(payment);
        log.info("Payment created id={}, userId={}, type={}, amount={}",
                saved.getId(), user.getId(), saved.getTransferType(), saved.getAmount());

        return paymentMapper.toResponse(saved);
    }
}