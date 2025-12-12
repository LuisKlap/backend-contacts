package com.dev.contacts.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Token é obrigatório") String token,

    @NotBlank(message = "Nova senha é obrigatória") @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres") String newPassword) {
}
