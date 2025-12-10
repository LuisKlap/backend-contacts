package com.dev.contacts.dto.auth;

import lombok.Builder;

@Builder
public record ResetPasswordResponse(
    String message) {
}
