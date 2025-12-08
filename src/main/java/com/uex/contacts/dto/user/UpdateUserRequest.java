package com.uex.contacts.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "fullName é obrigatório") @Size(max = 150, message = "fullName deve ter no máximo 150 caracteres") String fullName,

    @NotBlank(message = "email é obrigatório") @Email(message = "email inválido") @Size(max = 255, message = "email deve ter no máximo 255 caracteres") String email,

    @Size(min = 6, message = "password deve ter no mínimo 6 caracteres") String password,

    String currentPassword) {
}
