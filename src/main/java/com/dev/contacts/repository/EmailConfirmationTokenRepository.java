package com.dev.contacts.repository;

import com.dev.contacts.entity.EmailConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {
  Optional<EmailConfirmationToken> findByToken(String token);

  void deleteByUserId(Long userId);
}
