package com.clayfin.training.minirib.repository;

import com.clayfin.training.minirib.domain.CifAccount;
import com.clayfin.training.minirib.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CifAccountRepository extends JpaRepository<CifAccount, Long> {

    List<CifAccount> findByCifOrderByAccNumberAsc(String cif);
    boolean existsByCifAndAccNumberAndStatus(String cif, String accNumber, AccountStatus status);
}