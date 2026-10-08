package com.clayfin.training.minirib.domain;

import com.clayfin.training.minirib.enums.AccountDtype;
import com.clayfin.training.minirib.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fnd_corp_cif_account")
public class CifAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, length = 20)
    private String cif;

    @Column(name = "acc_number", nullable = false, unique = true, length = 20)
    private String accNumber;

    @Column(name = "acc_holder_name", nullable = false, length = 100)
    private String accHolderName;

    @Column(name = "curr_code", nullable = false, length = 3)
    private String currCode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private AccountDtype dtype;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private AccountStatus status;
}