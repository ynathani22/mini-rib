package com.clayfin.training.minirib.repository;

import com.clayfin.training.minirib.domain.CifAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CifAccountRepository extends JpaRepository<CifAccount, Long> {

    List<CifAccount> findByCifOrderByAccNumberAsc(String cif);
}