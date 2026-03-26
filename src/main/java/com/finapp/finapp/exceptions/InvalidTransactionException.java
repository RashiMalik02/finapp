package com.finapp.finapp.exceptions;

public class InvalidTransactionException extends RuntimeException{
    public InvalidTransactionException(String error) {
        super(error);
    }
}