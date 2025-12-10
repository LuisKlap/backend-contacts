package com.dev.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BadRequestException Tests")
class BadRequestExceptionTest {

  @Test
  @DisplayName("Deve criar exceção sem mensagem")
  void shouldCreateExceptionWithoutMessage() {
    BadRequestException exception = new BadRequestException();
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "Requisição inválida";
    BadRequestException exception = new BadRequestException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Requisição inválida";
    Throwable cause = new IllegalArgumentException("Argumento inválido");
    BadRequestException exception = new BadRequestException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    BadRequestException exception = new BadRequestException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
