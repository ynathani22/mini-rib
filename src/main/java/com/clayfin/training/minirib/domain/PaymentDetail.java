package com.clayfin.training.minirib.domain;

import com.clayfin.training.minirib.enums.TransferType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pay_payment_detail")
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String remarks;

    @Column(name = "debit_account", nullable = false, length = 12)
    private String debitAccount;

    @Column(name = "credit_account", nullable = false, length = 12)
    private String creditAccount;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "transfer_type", nullable = false, length = 10)
    private TransferType transferType;
}