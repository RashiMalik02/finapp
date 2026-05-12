package com.finapp.finapp.audit_dashboard.service;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.auth_users.dtos.UserDTO;
import com.finapp.finapp.transactions.dtos.TransactionDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AuditorService {
    Map<String , Long> getSystemTotals();

    Optional<UserDTO> findUserByEmail(String email);

    Optional<AccountDTO> findAccountDetailsByAccountNumber(String accountNumber);

    List<TransactionDTO> findTransactionsByAccountNumber(String accountNumber);

    Optional<TransactionDTO> findTransactionById(Long transactionId);
}
