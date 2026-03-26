package com.finapp.finapp.transactions.repo;

import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.transactions.entity.Transaction;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccount_AccountNumber(String accountNumber , Pageable pageable);

    List<Transaction> findByAccount_AccountNumber(String accountNumber);
//
//    List<Transaction> findByAccount(Account account);
}
