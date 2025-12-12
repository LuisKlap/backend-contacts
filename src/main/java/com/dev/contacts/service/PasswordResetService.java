package com.dev.contacts.service;

import com.dev.contacts.dto.auth.ForgotPasswordRequest;
import com.dev.contacts.dto.auth.ForgotPasswordResponse;
import com.dev.contacts.dto.auth.ResetPasswordRequest;
import com.dev.contacts.dto.auth.ResetPasswordResponse;
import com.dev.contacts.entity.PasswordResetToken;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.BadRequestException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.repository.PasswordResetTokenRepository;
import com.dev.contacts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final JavaMailSender mailSender;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Value("${app.password-reset.token-expiration-minutes:30}")
  private int tokenExpirationMinutes;

  @Transactional
  public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o email: " + request.email()));

    // Invalidar tokens anteriores não usados
    passwordResetTokenRepository.deleteUnusedTokensByUser(user);

    // Gerar novo token
    String token = UUID.randomUUID().toString();
    OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(tokenExpirationMinutes);

    PasswordResetToken resetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiresAt(expiresAt)
        .used(false)
        .build();

    passwordResetTokenRepository.save(resetToken);

    // Enviar email
    sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);

    log.info("Password reset requested for user: {}", user.getEmail());

    return ForgotPasswordResponse.builder()
        .message("Email de recuperação de senha enviado com sucesso")
        .email(user.getEmail())
        .build();
  }

  @Transactional
  public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
        .orElseThrow(() -> new BadRequestException("Token inválido ou expirado"));

    if (!resetToken.isValid()) {
      throw new BadRequestException("Token inválido ou expirado");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    // Marcar token como usado
    resetToken.setUsed(true);
    resetToken.setUsedAt(OffsetDateTime.now());
    passwordResetTokenRepository.save(resetToken);

    log.info("Password reset successfully for user: {}", user.getEmail());

    return ResetPasswordResponse.builder()
        .message("Senha alterada com sucesso")
        .build();
  }

  private void sendPasswordResetEmail(String toEmail, String fullName, String token) {
    try {
      String resetLink = frontendUrl + "/reset-password?token=" + token;

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom("noreply@contatos.com");
      message.setTo(toEmail);
      message.setSubject("Recuperação de Senha - Sistema de Contatos");
      message.setText(String.format(
          """
              Olá %s,

              Você solicitou a recuperação de senha para sua conta no Sistema de Contatos.

              Para criar uma nova senha, clique no link abaixo:
              %s

              Este link é válido por %d minutos.

              Se você não solicitou esta recuperação, ignore este email. Sua senha permanecerá inalterada.

              Atenciosamente,
              Equipe Sistema de Contatos
              """,
          fullName,
          resetLink,
          tokenExpirationMinutes));

      mailSender.send(message);

      log.info("Password reset email sent successfully to: {}", toEmail);

    } catch (Exception e) {
      log.error("Failed to send password reset email to: {}", toEmail, e);
      throw new RuntimeException("Falha ao enviar email de recuperação de senha", e);
    }
  }

  @Transactional
  public void cleanupExpiredTokens() {
    passwordResetTokenRepository.deleteExpiredTokens(OffsetDateTime.now());
    log.info("Expired password reset tokens cleaned up");
  }
}
