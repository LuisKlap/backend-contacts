package com.dev.contacts.dto.auth;

import lombok.Builder;

/**
 * Response para solicitação de recuperação de senha.
 * 
 * Por segurança, não revela se o email existe no sistema.
 */
@Builder
public record ForgotPasswordResponse(
        String message) {
}
