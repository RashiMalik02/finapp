package com.finapp.finapp.audit_dashboard.service;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.account.repo.AccountRepo;
import com.finapp.finapp.auth_users.dtos.UserDTO;
import com.finapp.finapp.auth_users.repo.UserRepo;
import com.finapp.finapp.transactions.dtos.TransactionDTO;
import com.finapp.finapp.transactions.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditorServiceImpl implements AuditorService {

    private final UserRepo userRepo;
    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;
    private final ModelMapper mapper;

    @Override
    public Map<String, Long> getSystemTotals() {
        long totalUsers = userRepo.count();
        long totalAccounts = accountRepo.count();
        long totalTransactions = transactionRepo.count();
        return Map.of(
                "totalUsers", totalUsers,
                "totalAccounts", totalAccounts,
                "totalTransactions", totalTransactions
        );
    }

    @Override
    public Optional<UserDTO> findUserByEmail(String email) {
        return userRepo.findByEmail(email).map(user -> mapper.map(user, UserDTO.class));
    }

    @Override
    public Optional<AccountDTO> findAccountDetailsByAccountNumber(String accountNumber) {
        return accountRepo.findByAccountNumber(accountNumber).map(acc -> mapper.map(acc, AccountDTO.class));
    }

    @Override
    public List<TransactionDTO> findTransactionsByAccountNumber(String accountNumber) {
        return transactionRepo.findByAccount_AccountNumber(accountNumber).stream()
                .map(tra -> mapper.map(tra, TransactionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TransactionDTO> findTransactionById(Long transactionId) {
        return transactionRepo.findById(transactionId).map(tra -> mapper.map(tra , TransactionDTO.class));
    }
}
