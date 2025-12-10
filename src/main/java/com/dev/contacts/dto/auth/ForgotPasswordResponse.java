package com.dev.contacts.dto.auth;

import lombok.Builder;

@Builder
public record ForgotPasswordResponse(
    String message,
    String email) {
}
