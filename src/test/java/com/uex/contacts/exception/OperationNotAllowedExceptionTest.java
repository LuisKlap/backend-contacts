package com.uex.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OperationNotAllowedException Tests")
class OperationNotAllowedExceptionTest {

  @Test
  @DisplayName("Deve criar exceção sem mensagem")
  void shouldCreateExceptionWithoutMessage() {
    OperationNotAllowedException exception = new OperationNotAllowedException();
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "Operação não permitida";
    OperationNotAllowedException exception = new OperationNotAllowedException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Usuário sem permissão";
    Throwable cause = new RuntimeException("Causa raiz");
    OperationNotAllowedException exception = new OperationNotAllowedException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    OperationNotAllowedException exception = new OperationNotAllowedException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
