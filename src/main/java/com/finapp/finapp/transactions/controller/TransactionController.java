package com.finapp.finapp.transactions.controller;

import com.finapp.finapp.res.Response;
import com.finapp.finapp.transactions.dtos.TransactionRequest;
import com.finapp.finapp.transactions.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Response<?>> createTransaction(@RequestBody @Valid TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.createTransaction(transactionRequest));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Response<?>> getTrasactionsForMyAccount(@PathVariable String accountNumber,
                                                                  @RequestParam(defaultValue = "0") int page ,
                                                                  @RequestParam(defaultValue = "0") int size) {
        return ResponseEntity.ok(transactionService.getTransactionsForAnAccount(accountNumber, page , size));
    }
}
