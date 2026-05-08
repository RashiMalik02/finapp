package com.finapp.finapp.transactions.services;

import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.account.repo.AccountRepo;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.auth_users.services.UserService;
import com.finapp.finapp.enums.TransactionStatus;
import com.finapp.finapp.enums.TransactionType;
import com.finapp.finapp.exceptions.BadRequestException;
import com.finapp.finapp.exceptions.InsufficientBalanceException;
import com.finapp.finapp.exceptions.InvalidTransactionException;
import com.finapp.finapp.exceptions.NotFoundException;
import com.finapp.finapp.notification.dtos.NotificationDTO;
import com.finapp.finapp.notification.services.NotificationService;
import com.finapp.finapp.res.Response;
import com.finapp.finapp.transactions.dtos.TransactionDTO;
import com.finapp.finapp.transactions.dtos.TransactionRequest;
import com.finapp.finapp.transactions.entity.Transaction;
import com.finapp.finapp.transactions.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
@Slf4j

public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();

        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDescription(transactionRequest.getDescription());

        switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> handleDeposit(transactionRequest, transaction);
            case TRANSFER -> handleTransfer(transactionRequest, transaction);
            case WITHDRAWAL -> handleWithdrawal(transactionRequest, transaction);
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepo.save(transaction);

        sendTransactionNotification(savedTransaction);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transaction successfull!!")
                .build();
    }

    @Override
    public Response<List<TransactionDTO>> getTransactionsForAnAccount(String accountNumber, int page, int size) {
        User user = userService.getCurrentLoggedInUser();

        Account account = accountRepo.findByAccountNumber(accountNumber).orElseThrow(() -> new NotFoundException("Account not found!!"));

        if(!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("The account does not belong to authenticated user");
        }

        Pageable pageable = PageRequest.of(page , size, Sort.by("transactionDate").descending());
        Page<Transaction> txns = transactionRepo.findByAccount_AccountNumber(accountNumber, pageable);

        List<TransactionDTO> list = txns.getContent().stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();

        return Response.<List<TransactionDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions retrieved")
                .data(list)
                .meta(Map.of(
                        "currentPage" , txns.getNumber(),
                        "totalItems", txns.getTotalElements(),
                        "totalPages", txns.getTotalPages(),
                        "pageSize", txns.getSize()
                ))
                .build();
    }

    private void handleDeposit(TransactionRequest request, Transaction transaction) {
        Account account = accountRepo.findByAccountNumber(request.getAccountNumber()).orElseThrow(() -> new NotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));
        transaction.setAccount(account);
        accountRepo.save(account);
    }

    private void handleWithdrawal(TransactionRequest request, Transaction transaction) {
        Account account = accountRepo.findByAccountNumber(request.getAccountNumber()).orElseThrow(() -> new NotFoundException("Account not found!!"));

        if(account.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        transaction.setAccount(account);
        accountRepo.save(account);
    }

    private void handleTransfer(TransactionRequest request, Transaction transaction) {
        Account sourceAccount = accountRepo.findByAccountNumber(request.getAccountNumber()).orElseThrow(() -> new NotFoundException("Account not found!!"));
        Account destinationAccount = accountRepo.findByAccountNumber(request.getDestinationAccountNumber()).orElseThrow(() -> new NotFoundException("Destination Account not found!!"));

        if(sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        //deducting from source account
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepo.save(sourceAccount);

        //credited to destination account
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        accountRepo.save(destinationAccount);

        transaction.setAccount(sourceAccount);
        transaction.setSourceAccount(sourceAccount.getAccountNumber());
        transaction.setDestinationAccount(destinationAccount.getAccountNumber());


    }

    private void sendTransactionNotification(Transaction t) {
        User user = t.getAccount().getUser();
        String subject;
        String template;

        Map<String , Object> vars = new HashMap<>();

        vars.put("name", user.getFirstName());
        vars.put("amount", t.getAmount());
        vars.put("accountNumber", t.getAccount().getAccountNumber());
        vars.put("date", t.getTransactionDate());
        vars.put("balance", t.getAccount().getBalance());

        if(t.getTransactionType() == TransactionType.DEPOSIT) {
            subject = "Credit Alert!";
            template = "credit-alert";

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(vars)
                    .build();
            notificationService.sendEmail(notificationDTO, user);
        } else if (t.getTransactionType() == TransactionType.WITHDRAWAL) {
            subject = "Debit Alert!";
            template = "debit-alert";

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(vars)
                    .build();
            notificationService.sendEmail(notificationDTO, user);
        } else if (t.getTransactionType() == TransactionType.TRANSFER) {
            subject = "Debit Alert!";
            template = "debit-alert";

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(vars)
                    .build();
            notificationService.sendEmail(notificationDTO, user);

            Account destinationAccount = accountRepo.findByAccountNumber(t.getDestinationAccount()).orElseThrow(() -> new NotFoundException("Destination Account not found!!"));

            User reciever = destinationAccount.getUser();

            Map<String , Object> rcvrVars = new HashMap<>();

            rcvrVars.put("name", reciever.getFirstName());
            rcvrVars.put("amount", t.getAmount());
            rcvrVars.put("accountNumber", t.getAccount().getAccountNumber());
            rcvrVars.put("date", t.getTransactionDate());
            rcvrVars.put("balance", t.getAccount().getBalance());

            NotificationDTO notificationDTOForRcvrs = NotificationDTO.builder()
                    .recipient(reciever.getEmail())
                    .subject("Credit Alert!")
                    .templateName("credit-alert")
                    .templateVariables(rcvrVars)
                    .build();
            notificationService.sendEmail(notificationDTOForRcvrs, reciever);
        }
    }

}
