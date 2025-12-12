package com.dev.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

  @Test
  @DisplayName("Deve criar exceção sem mensagem")
  void shouldCreateExceptionWithoutMessage() {
    ResourceNotFoundException exception = new ResourceNotFoundException();
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem")
  void shouldCreateExceptionWithMessage() {
    String message = "Recurso não encontrado";
    ResourceNotFoundException exception = new ResourceNotFoundException(message);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("Deve criar exceção com mensagem e causa")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Recurso não encontrado";
    Throwable cause = new RuntimeException("Causa raiz");
    ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("Deve ser uma RuntimeException")
  void shouldBeRuntimeException() {
    ResourceNotFoundException exception = new ResourceNotFoundException("Test");
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}
