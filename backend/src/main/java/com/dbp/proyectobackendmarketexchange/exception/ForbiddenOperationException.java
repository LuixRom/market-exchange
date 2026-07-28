package com.dbp.proyectobackendmarketexchange.exception;

public class ForbiddenOperationException extends RuntimeException {
  public ForbiddenOperationException(String message) {
    super(message);
  }
}
