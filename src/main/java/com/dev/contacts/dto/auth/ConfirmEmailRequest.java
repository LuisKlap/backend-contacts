package com.dev.contacts.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(
    @NotBlank String token) {
}
