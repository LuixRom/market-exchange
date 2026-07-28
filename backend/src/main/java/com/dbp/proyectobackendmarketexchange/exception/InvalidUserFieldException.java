package com.dbp.proyectobackendmarketexchange.exception;

public class InvalidUserFieldException extends RuntimeException {
    public InvalidUserFieldException(String message) {
        super(message);
    }
}