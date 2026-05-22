package com.finapp.finapp.account.controller;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.account.services.AccountService;
import com.finapp.finapp.res.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<Response<List<AccountDTO>>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @DeleteMapping("/close/{accountNumber}")
    public ResponseEntity<Response<?>> closeAccounts(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.closeAccount(accountNumber));
    }

    @GetMapping("/verify")
    public ResponseEntity<Response<AccountDTO>> verifyAccount(@RequestParam String accountNumber) {
        return ResponseEntity.ok(accountService.verifyAccount(accountNumber));
    }
}
