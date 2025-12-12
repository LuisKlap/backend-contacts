package com.dev.contacts.repository;

import com.dev.contacts.entity.PasswordResetToken;
import com.dev.contacts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByToken(String token);

  @Modifying
  @Query("DELETE FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false")
  void deleteUnusedTokensByUser(User user);

  @Modifying
  @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
  void deleteExpiredTokens(OffsetDateTime now);
}
