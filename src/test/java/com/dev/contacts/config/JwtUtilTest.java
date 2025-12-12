package com.dev.contacts.config;

import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", "my-super-secret-key-for-jwt-token-generation-at-least-256-bits");
    ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    jwtUtil.init();
  }

  @Test
  void shouldGenerateToken() {
    String username = "testuser";

    String token = jwtUtil.generateToken(username);

    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void shouldValidateValidToken() {
    String username = "testuser";
    String token = jwtUtil.generateToken(username);

    boolean isValid = jwtUtil.validateToken(token);

    assertThat(isValid).isTrue();
  }

  @Test
  void shouldNotValidateInvalidToken() {
    String invalidToken = "invalid.jwt.token";

    boolean isValid = jwtUtil.validateToken(invalidToken);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldNotValidateMalformedToken() {
    String malformedToken = "malformed-token";

    boolean isValid = jwtUtil.validateToken(malformedToken);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldNotValidateExpiredToken() {
    JwtUtil expiredJwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(expiredJwtUtil, "secret",
        "my-super-secret-key-for-jwt-token-generation-at-least-256-bits");
    ReflectionTestUtils.setField(expiredJwtUtil, "expirationMs", -1000L);
    expiredJwtUtil.init();

    String token = expiredJwtUtil.generateToken("testuser");

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    boolean isValid = jwtUtil.validateToken(token);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldNotValidateTokenWithDifferentSecret() {
    String username = "testuser";
    String token = jwtUtil.generateToken(username);

    JwtUtil differentJwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(differentJwtUtil, "secret",
        "different-super-secret-key-for-jwt-token-generation-at-least-256");
    ReflectionTestUtils.setField(differentJwtUtil, "expirationMs", 3600000L);
    differentJwtUtil.init();

    boolean isValid = differentJwtUtil.validateToken(token);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldExtractUsernameFromToken() {
    String username = "testuser";
    String token = jwtUtil.generateToken(username);

    String extractedUsername = jwtUtil.getUsernameFromToken(token);

    assertThat(extractedUsername).isEqualTo(username);
  }

  @Test
  void shouldExtractUsernameFromTokenWithSpecialCharacters() {
    String username = "test.user@example.com";
    String token = jwtUtil.generateToken(username);

    String extractedUsername = jwtUtil.getUsernameFromToken(token);

    assertThat(extractedUsername).isEqualTo(username);
  }

  @Test
  void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
    String invalidToken = "invalid.jwt.token";

    assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(invalidToken))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void shouldGenerateTokenWithCorrectClaims() {
    String username = "testuser";
    String token = jwtUtil.generateToken(username);

    String extractedUsername = jwtUtil.getUsernameFromToken(token);

    assertThat(extractedUsername).isEqualTo(username);
    assertThat(jwtUtil.validateToken(token)).isTrue();
  }

  @Test
  void shouldGenerateDifferentTokensForSameUser() {
    String username = "testuser";

    String token1 = jwtUtil.generateToken(username);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    String token2 = jwtUtil.generateToken(username);

    assertThat(token1).isNotEqualTo(token2);
    assertThat(jwtUtil.getUsernameFromToken(token1)).isEqualTo(username);
    assertThat(jwtUtil.getUsernameFromToken(token2)).isEqualTo(username);
  }

  @Test
  void shouldNotValidateNullToken() {
    boolean isValid = jwtUtil.validateToken(null);

    assertThat(isValid).isFalse();
  }

  @Test
  void shouldNotValidateEmptyToken() {
    boolean isValid = jwtUtil.validateToken("");

    assertThat(isValid).isFalse();
  }
}
