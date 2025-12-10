package com.dev.contacts.dto.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailResponse Tests")
class EmailResponseTest {

  @Test
  @DisplayName("Deve criar EmailResponse com sucesso")
  void shouldCreateEmailResponseSuccessfully() {
    EmailResponse response = EmailResponse.builder()
        .message("Email sent successfully")
        .to("test@example.com")
        .subject("Test Subject")
        .success(true)
        .build();

    assertThat(response.getMessage()).isEqualTo("Email sent successfully");
    assertThat(response.getTo()).isEqualTo("test@example.com");
    assertThat(response.getSubject()).isEqualTo("Test Subject");
    assertThat(response.isSuccess()).isTrue();
  }

  @Test
  @DisplayName("Deve criar EmailResponse com falha")
  void shouldCreateEmailResponseWithFailure() {
    EmailResponse response = EmailResponse.builder()
        .message("Failed to send email")
        .to("test@example.com")
        .subject("Test Subject")
        .success(false)
        .build();

    assertThat(response.getMessage()).isEqualTo("Failed to send email");
    assertThat(response.getTo()).isEqualTo("test@example.com");
    assertThat(response.getSubject()).isEqualTo("Test Subject");
    assertThat(response.isSuccess()).isFalse();
  }

  @Test
  @DisplayName("Deve usar construtor vazio")
  void shouldUseNoArgsConstructor() {
    EmailResponse response = new EmailResponse();
    response.setMessage("Test message");
    response.setTo("test@example.com");
    response.setSubject("Test");
    response.setSuccess(true);

    assertThat(response.getMessage()).isEqualTo("Test message");
    assertThat(response.getTo()).isEqualTo("test@example.com");
    assertThat(response.getSubject()).isEqualTo("Test");
    assertThat(response.isSuccess()).isTrue();
  }

  @Test
  @DisplayName("Deve usar construtor com todos os argumentos")
  void shouldUseAllArgsConstructor() {
    EmailResponse response = new EmailResponse(
        "Test message",
        "test@example.com",
        "Test Subject",
        true);

    assertThat(response.getMessage()).isEqualTo("Test message");
    assertThat(response.getTo()).isEqualTo("test@example.com");
    assertThat(response.getSubject()).isEqualTo("Test Subject");
    assertThat(response.isSuccess()).isTrue();
  }
}
