package com.dbp.proyectobackendmarketexchange.exception;

public class RatingNotAllowedException extends RuntimeException {
    public RatingNotAllowedException(String message) {
        super(message);
    }
}
