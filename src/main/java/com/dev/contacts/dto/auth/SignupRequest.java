package com.dev.contacts.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para registro de novo usuário.
 */
public record SignupRequest(
    @NotBlank(message = "Nome completo é obrigatório") @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres") String fullName,

    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Size(max = 255, message = "Email deve ter no máximo 255 caracteres") String email,

    @NotBlank(message = "Senha é obrigatória") @Size(min = 8, max = 255, message = "Senha deve ter entre 8 e 255 caracteres") String password) {
}
