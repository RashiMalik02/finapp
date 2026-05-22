package com.finapp.finapp.account.services;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.enums.AccountType;
import com.finapp.finapp.res.Response;

import java.util.List;

public interface AccountService {
    Account createAccount(AccountType accountType, User user);

    Response<List<AccountDTO>> getMyAccounts();

    Response<?> closeAccount(String accountNumber);

    Response<AccountDTO> verifyAccount(String accountNumber);
}
