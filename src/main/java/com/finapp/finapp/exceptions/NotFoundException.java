package com.finapp.finapp.exceptions;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String error) {
        super(error);
    }
}
