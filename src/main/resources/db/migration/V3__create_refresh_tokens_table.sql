-- Tabela de refresh tokens para implementar autenticação JWT com refresh token
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índice para melhorar performance nas buscas por token
CREATE INDEX idx_refresh_token_token ON refresh_tokens(token);

-- Índice para melhorar performance nas buscas por usuário
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);

-- Índice para melhorar performance na limpeza de tokens expirados
CREATE INDEX idx_refresh_token_expires_at ON refresh_tokens(expires_at);
