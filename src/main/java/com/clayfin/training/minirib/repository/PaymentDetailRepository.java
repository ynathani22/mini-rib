package com.clayfin.training.minirib.repository;

import com.clayfin.training.minirib.domain.PaymentDetail;
import com.clayfin.training.minirib.enums.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

    List<PaymentDetail> findByCreatedByAndTransferTypeAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(
            Long createdBy, TransferType transferType, LocalDate fromDate, LocalDate toDate);
}