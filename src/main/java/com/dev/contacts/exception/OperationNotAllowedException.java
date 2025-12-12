package com.dev.contacts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Erros de autorização de negócio (operação proibida no contexto do usuário).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class OperationNotAllowedException extends RuntimeException {
  public OperationNotAllowedException() {
    super();
  }

  public OperationNotAllowedException(String message) {
    super(message);
  }

  public OperationNotAllowedException(String message, Throwable cause) {
    super(message, cause);
  }
}
