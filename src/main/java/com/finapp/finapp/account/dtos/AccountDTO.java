package com.finapp.finapp.account.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.finapp.finapp.auth_users.dtos.UserDTO;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.enums.AccountStatus;
import com.finapp.finapp.enums.AccountType;
import com.finapp.finapp.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    private Long id;

    private String accountNumber;

    private BigDecimal balance;
    private AccountType accountType;

    @JsonBackReference //this will not be added to the account STO. It will be ignored because it is a back reference
    private UserDTO user;

    private Currency currency;

    private AccountStatus accountStatus;

    @JsonManagedReference //it helps avoid recursion loop by ignoring the userDTO within the account DTO
    private List<TransactionDTO> transactions;

    private LocalDateTime closedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
