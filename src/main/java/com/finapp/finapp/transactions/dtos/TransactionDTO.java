package com.finapp.finapp.transactions.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.enums.TransactionStatus;
import com.finapp.finapp.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private Long id;

    private BigDecimal amount;

    private TransactionType transactionType;

    private LocalDateTime transactionDate ;

    private String description;

    private TransactionStatus status;

    @JsonBackReference
    private AccountDTO account;

    private String sourceAccount;
    private String destinationAccount;
}
