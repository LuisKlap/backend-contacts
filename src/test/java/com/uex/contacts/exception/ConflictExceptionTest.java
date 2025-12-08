package com.uex.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConflictException Tests")
class ConflictExceptionTest {

  @Test
  @DisplayName("Deve criar exceção sem mensagem")
  void shouldCreateExceptionWithoutMessage() {
    ConflictException exception = new ConflictException();
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "E-mail já cadastrado";
    ConflictException exception = new ConflictException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "CPF duplicado";
    Throwable cause = new RuntimeException("Causa raiz");
    ConflictException exception = new ConflictException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    ConflictException exception = new ConflictException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
