-- Adiciona campo para controle de verificação de e-mail
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;