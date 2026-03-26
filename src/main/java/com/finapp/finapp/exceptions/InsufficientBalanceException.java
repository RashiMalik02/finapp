package com.finapp.finapp.exceptions;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String error) {
        super(error);
    }
}
