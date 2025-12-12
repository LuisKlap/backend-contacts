package com.dev.contacts.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para autenticação de usuário.
 */
public record LoginRequest(
    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,

    @NotBlank(message = "Senha é obrigatória") @Size(min = 1, max = 255, message = "Senha inválida") String password) {
}
