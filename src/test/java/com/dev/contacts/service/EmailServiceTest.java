package com.dev.contacts.service;

import com.dev.contacts.dto.email.EmailRequest;
import com.dev.contacts.dto.email.EmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailService emailService;

  private EmailRequest emailRequest;

  @BeforeEach
  void setUp() {
    emailRequest = EmailRequest.builder()
        .to("test@example.com")
        .subject("Test Subject")
        .build();
  }

  @Test
  @DisplayName("Deve enviar email com sucesso")
  void shouldSendEmailSuccessfully() {
    doNothing().when(mailSender).send(any(SimpleMailMessage.class));

    EmailResponse response = emailService.sendEmail(emailRequest);

    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getTo()).isEqualTo(emailRequest.getTo());
    assertThat(response.getSubject()).isEqualTo(emailRequest.getSubject());
    assertThat(response.getMessage()).isEqualTo("Email sent successfully");

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("Deve retornar falha ao enviar email com erro")
  void shouldReturnFailureWhenSendingEmailFails() {
    doThrow(new RuntimeException("Mail server error"))
        .when(mailSender).send(any(SimpleMailMessage.class));

    EmailResponse response = emailService.sendEmail(emailRequest);

    assertThat(response).isNotNull();
    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getTo()).isEqualTo(emailRequest.getTo());
    assertThat(response.getSubject()).isEqualTo(emailRequest.getSubject());
    assertThat(response.getMessage()).contains("Failed to send email");

    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}
