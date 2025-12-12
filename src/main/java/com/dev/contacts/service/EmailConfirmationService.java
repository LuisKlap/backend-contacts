package com.dev.contacts.service;

import com.dev.contacts.entity.EmailConfirmationToken;
import com.dev.contacts.entity.User;
import com.dev.contacts.repository.EmailConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
  private final EmailConfirmationTokenRepository tokenRepository;

  public EmailConfirmationToken createToken(User user, int minutesToExpire) {
    // Remove tokens antigos
    tokenRepository.deleteByUserId(user.getId());
    EmailConfirmationToken token = EmailConfirmationToken.builder()
        .token(UUID.randomUUID().toString())
        .user(user)
        .expiresAt(OffsetDateTime.now().plusMinutes(minutesToExpire))
        .used(false)
        .build();
    return tokenRepository.save(token);
  }

  public Optional<EmailConfirmationToken> findByToken(String token) {
    return tokenRepository.findByToken(token);
  }

  public void markAsUsed(EmailConfirmationToken token) {
    token.setUsed(true);
    token.setUsedAt(OffsetDateTime.now());
    tokenRepository.save(token);
  }
}
