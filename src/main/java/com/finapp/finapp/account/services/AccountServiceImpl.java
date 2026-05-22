package com.finapp.finapp.account.services;

import com.finapp.finapp.account.dtos.AccountDTO;
import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.account.repo.AccountRepo;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.auth_users.services.UserService;
import com.finapp.finapp.enums.AccountStatus;
import com.finapp.finapp.enums.AccountType;
import com.finapp.finapp.enums.Currency;
import com.finapp.finapp.exceptions.BadRequestException;
import com.finapp.finapp.exceptions.NotFoundException;
import com.finapp.finapp.res.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService{

    private final AccountRepo accountRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private final Random random = new Random();

    @Override
    public Account createAccount(AccountType accountType, User user) {
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .createdAt(LocalDateTime.now())
                .balance(BigDecimal.ZERO)
                .accountStatus(AccountStatus.ACTIVE)
                .user(user)
                .currency(Currency.USD)
                .build();
        return accountRepo.save(account);
    }

    @Override
    public Response<List<AccountDTO>> getMyAccounts() {
        User user = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepo.findByUserId(user.getId())
                .stream()
                .map(acc -> modelMapper.map(acc, AccountDTO.class))
                .toList();

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User accounts fetched successfully!!")
                .data(accounts)
                .build();
    }

    @Override
    public Response<?> closeAccount(String accountNumber) {
        User user = userService.getCurrentLoggedInUser();
        Account account = accountRepo.findByAccountNumber(accountNumber).orElseThrow(() -> new NotFoundException("Account with account number does not exist"));

        if(!user.getAccounts().contains(account)) {
            throw new NotFoundException("This account does not belong to you");
        }

        if(account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }

        account.setAccountStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());

        accountRepo.save(account);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account Closed permanently!")
                .build();
    }

    @Override
    public Response<AccountDTO> verifyAccount(String accountNumber) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);

        return Response.<AccountDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account found")
                .data(accountDTO)
                .build();
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "24" + (random.nextInt(90000000) + 10000000);
        } while (accountRepo.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }
}
