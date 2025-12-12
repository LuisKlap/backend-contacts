package com.dev.contacts.dto.email;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailRequest Tests")
class EmailRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("Deve criar EmailRequest válido")
  void shouldCreateValidEmailRequest() {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject("Test Subject")
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
    assertThat(request.getTo()).isEqualTo("test@example.com");
    assertThat(request.getSubject()).isEqualTo("Test Subject");
  }

  @Test
  @DisplayName("Deve falhar quando email é inválido")
  void shouldFailWhenEmailIsInvalid() {
    EmailRequest request = EmailRequest.builder()
        .to("invalid-email")
        .subject("Test Subject")
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
  }

  @Test
  @DisplayName("Deve falhar quando email está em branco")
  void shouldFailWhenEmailIsBlank() {
    EmailRequest request = EmailRequest.builder()
        .to("")
        .subject("Test Subject")
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  @DisplayName("Deve falhar quando subject está em branco")
  void shouldFailWhenSubjectIsBlank() {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject("")
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Subject is required");
  }

  @Test
  @DisplayName("Deve falhar quando to é null")
  void shouldFailWhenToIsNull() {
    EmailRequest request = EmailRequest.builder()
        .to(null)
        .subject("Test Subject")
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  @DisplayName("Deve falhar quando subject é null")
  void shouldFailWhenSubjectIsNull() {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject(null)
        .build();

    Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

    assertThat(violations).isNotEmpty();
  }
}
