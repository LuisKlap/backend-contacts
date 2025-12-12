-- Adiciona campos de autenticação de dois fatores à tabela users
ALTER TABLE users
  ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN two_factor_secret VARCHAR(64),
  ADD COLUMN two_factor_last_sent TIMESTAMP WITH TIME ZONE,
  ADD COLUMN two_factor_temp_code VARCHAR(8),
  ADD COLUMN two_factor_temp_code_expiry TIMESTAMP WITH TIME ZONE,
  ADD COLUMN two_factor_type VARCHAR(16) DEFAULT 'email';
