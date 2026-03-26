package com.finapp.finapp.exceptions;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String error) {
        super(error);
    }
}
