package com.dev.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExternalServiceException Tests")
class ExternalServiceExceptionTest {

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "Erro ao consultar serviço externo";
    ExternalServiceException exception = new ExternalServiceException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "ViaCEP indisponível";
    Throwable cause = new RuntimeException("Timeout");
    ExternalServiceException exception = new ExternalServiceException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    ExternalServiceException exception = new ExternalServiceException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
