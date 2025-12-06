package com.uex.contacts.dto.auth;

public record AuthResponse(
    String token,
    String tokenType,
    Long expiresIn) {
  public AuthResponse(String token, Long expiresIn) {
    this(token, "Bearer", expiresIn);
  }
}
