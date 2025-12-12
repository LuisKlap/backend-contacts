package com.dev.contacts.service;

import com.dev.contacts.dto.email.EmailRequest;
import com.dev.contacts.dto.email.EmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public EmailResponse sendEmail(EmailRequest request) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom("noreply@contatos.com");
      message.setTo(request.getTo());
      message.setSubject(request.getSubject());
      message.setText(request.getBody());

      mailSender.send(message);

      log.info("Email sent successfully to: {}", request.getTo());

      return EmailResponse.builder()
          .message("Email sent successfully")
          .to(request.getTo())
          .subject(request.getSubject())
          .success(true)
          .build();

    } catch (Exception e) {
      log.error("Failed to send email to: {}", request.getTo(), e);

      return EmailResponse.builder()
          .message("Failed to send email: " + e.getMessage())
          .to(request.getTo())
          .subject(request.getSubject())
          .success(false)
          .build();
    }
  }
}
