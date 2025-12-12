package com.dev.contacts.config;

import com.dev.contacts.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuração de tarefas agendadas da aplicação.
 * 
 * Gerencia execuções periódicas como limpeza de tokens expirados,
 * refresh tokens revogados, etc.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

  private final PasswordResetService passwordResetService;

  /**
   * Limpa tokens de recuperação de senha expirados do banco de dados.
   * Executa diariamente às 2h da manhã.
   */
  @Scheduled(cron = "${app.scheduling.cleanup-expired-tokens:0 0 2 * * *}")
  public void cleanupExpiredPasswordResetTokens() {
    log.info("Starting scheduled cleanup of expired password reset tokens");
    try {
      passwordResetService.cleanupExpiredTokens();
      log.info("Scheduled cleanup of expired password reset tokens completed successfully");
    } catch (Exception e) {
      log.error("Error during scheduled cleanup of password reset tokens", e);
    }
  }
}
