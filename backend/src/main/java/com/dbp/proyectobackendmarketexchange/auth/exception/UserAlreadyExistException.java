package com.dbp.proyectobackendmarketexchange.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String message) {super(message);}
}
