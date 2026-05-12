package com.finapp.finapp.audit_dashboard.controller;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.audit_dashboard.service.AuditorService;
import com.finapp.finapp.auth_users.dtos.UserDTO;
import com.finapp.finapp.transactions.dtos.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
@PreAuthorize(("hasAuthority('ADMIN') or hasAuthority('AUDITOR')"))
public class AuditorController {
    private final AuditorService auditorService;

    @GetMapping("/totals")
    public ResponseEntity<Map<String , Long>> getSystemTotals() {
        return ResponseEntity.ok(auditorService.getSystemTotals());
    }

    @GetMapping("/users")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        Optional<UserDTO> user = auditorService.findUserByEmail(email);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/accounts")
    public ResponseEntity<AccountDTO> getAccountDetailsByAccountNumber(@RequestParam String accountNumber) {
        Optional<AccountDTO> account = auditorService.findAccountDetailsByAccountNumber(accountNumber);

        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/transactions/by-account")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountNumber(@RequestParam String accountNumber) {
        List<TransactionDTO> transactions = auditorService.findTransactionsByAccountNumber(accountNumber);

        if (transactions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/by-id")
    public ResponseEntity<TransactionDTO> getAccountDetailsByAccountNumber(@RequestParam Long transactionId) {
        Optional<TransactionDTO> transaction = auditorService.findTransactionById(transactionId);

        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
