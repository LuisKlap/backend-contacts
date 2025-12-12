package com.dev.contacts.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para redefinição de senha usando token.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Token é obrigatório") @Size(min = 36, max = 36, message = "Token inválido") String token,

        @NotBlank(message = "Nova senha é obrigatória") @Size(min = 8, max = 255, message = "Senha deve ter entre 8 e 255 caracteres") String newPassword) {
}
