package com.dev.contacts.dto.auth;

/**
 * Response contendo tokens JWT de autenticação.
 * 
 * @param accessToken  Token de acesso JWT
 * @param refreshToken Token para renovação de acesso
 * @param tokenType    Tipo do token (sempre "Bearer")
 * @param expiresIn    Tempo de expiração em segundos
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn) {

  public AuthResponse(String accessToken, String refreshToken, Long expiresIn) {
    this(accessToken, refreshToken, "Bearer", expiresIn);
  }
}
