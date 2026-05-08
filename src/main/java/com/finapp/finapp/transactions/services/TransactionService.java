package com.finapp.finapp.transactions.services;

import com.finapp.finapp.res.Response;
import com.finapp.finapp.transactions.dtos.TransactionDTO;
import com.finapp.finapp.transactions.dtos.TransactionRequest;

import java.util.List;

public interface TransactionService {
    Response<?> createTransaction(TransactionRequest transactionRequest);

    Response<List<TransactionDTO>> getTransactionsForAnAccount(String accountNumber , int page , int size);
}
