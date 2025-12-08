package com.uex.contacts.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("Deve tratar Exception genérica")
  void shouldHandleGenericException() {
    Exception exception = new Exception("Erro genérico");

    ResponseEntity<Map<String, String>> response = handler.handleAll(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("error")).isEqualTo("Exception");
    assertThat(response.getBody().get("message")).isEqualTo("Erro genérico");
  }

  @Test
  @DisplayName("Deve tratar RuntimeException")
  void shouldHandleRuntimeException() {
    RuntimeException exception = new RuntimeException("Erro em tempo de execução");

    ResponseEntity<Map<String, String>> response = handler.handleAll(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("error")).isEqualTo("RuntimeException");
    assertThat(response.getBody().get("message")).isEqualTo("Erro em tempo de execução");
  }

  @Test
  @DisplayName("Deve tratar IllegalArgumentException")
  void shouldHandleIllegalArgumentException() {
    IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

    ResponseEntity<Map<String, String>> response = handler.handleAll(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("error")).isEqualTo("IllegalArgumentException");
    assertThat(response.getBody().get("message")).isEqualTo("Argumento inválido");
  }

  @Test
  @DisplayName("Deve incluir nome da classe da exceção no corpo da resposta")
  void shouldIncludeExceptionClassNameInResponse() {
    Exception exception = new NullPointerException("Objeto nulo");

    ResponseEntity<Map<String, String>> response = handler.handleAll(exception);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("error")).isEqualTo("NullPointerException");
  }

  @Test
  @DisplayName("Deve retornar mapa com chaves error e message")
  void shouldReturnMapWithErrorAndMessageKeys() {
    Exception exception = new Exception("Teste");

    ResponseEntity<Map<String, String>> response = handler.handleAll(exception);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsKeys("error", "message");
  }
}
