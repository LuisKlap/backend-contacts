package com.uex.contacts.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Nome completo é obrigatório") @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres") String fullName,

        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Size(max = 255, message = "Email deve ter no máximo 255 caracteres") String email,

        @NotBlank(message = "Senha é obrigatória") @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres") String password) {
}
