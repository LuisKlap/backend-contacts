package com.dev.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidCredentialsException Tests")
class InvalidCredentialsExceptionTest {

  @Test
  @DisplayName("Deve criar exceção sem mensagem")
  void shouldCreateExceptionWithoutMessage() {
    InvalidCredentialsException exception = new InvalidCredentialsException();
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "Credenciais inválidas";
    InvalidCredentialsException exception = new InvalidCredentialsException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Senha incorreta";
    Throwable cause = new RuntimeException("Causa raiz");
    InvalidCredentialsException exception = new InvalidCredentialsException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    InvalidCredentialsException exception = new InvalidCredentialsException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
