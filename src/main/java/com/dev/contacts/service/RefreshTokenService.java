package com.dev.contacts.service;

import com.dev.contacts.config.JwtUtil;
import com.dev.contacts.entity.RefreshToken;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.InvalidCredentialsException;
import com.dev.contacts.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Transactional
  public RefreshToken createRefreshToken(User user) {
    // Revoga tokens antigos do usuário (opcional, para limitar tokens ativos)
    // refreshTokenRepository.revokeAllUserTokens(user, OffsetDateTime.now());

    String token = jwtUtil.generateRefreshToken(user.getEmail());

    RefreshToken refreshToken = RefreshToken.builder()
        .token(token)
        .user(user)
        .expiresAt(OffsetDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationMs() / 1000))
        .revoked(false)
        .build();

    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional(readOnly = true)
  public RefreshToken validateRefreshToken(String token) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
        .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

    if (!refreshToken.isValid()) {
      throw new InvalidCredentialsException("Refresh token expired or revoked");
    }

    // Valida também com o JwtUtil
    if (!jwtUtil.validateToken(token)) {
      throw new InvalidCredentialsException("Invalid refresh token signature");
    }

    return refreshToken;
  }

  @Transactional
  public void revokeRefreshToken(String token) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
        .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found"));

    refreshToken.setRevoked(true);
    refreshToken.setRevokedAt(OffsetDateTime.now());
    refreshTokenRepository.save(refreshToken);
  }

  @Transactional
  public void revokeAllUserTokens(User user) {
    refreshTokenRepository.revokeAllUserTokens(user, OffsetDateTime.now());
  }

  // Limpa tokens expirados a cada 24 horas
  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional
  public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteExpiredTokens(OffsetDateTime.now());
  }
}
