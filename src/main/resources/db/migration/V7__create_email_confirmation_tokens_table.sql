-- Cria tabela para tokens de confirmação de e-mail
CREATE TABLE email_confirmation_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_email_confirmation_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_confirmation_token ON email_confirmation_tokens(token);
CREATE INDEX idx_email_confirmation_user_id ON email_confirmation_tokens(user_id);
