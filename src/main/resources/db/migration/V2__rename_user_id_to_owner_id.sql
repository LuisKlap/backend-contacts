-- V2__rename_user_id_to_owner_id.sql

-- Se tiver sobrado alguma constraint antiga, apaga
ALTER TABLE contacts DROP CONSTRAINT IF EXISTS contacts_user_cpf_unique;

-- Garante que a constraint uk_user_cpf existe, mesmo que tenha outro nome antes
ALTER TABLE contacts DROP CONSTRAINT IF EXISTS uk_user_cpf;
ALTER TABLE contacts ADD CONSTRAINT uk_user_cpf UNIQUE (owner_id, cpf);

-- Ajusta índices antigos, se existirem
DROP INDEX IF EXISTS idx_contacts_user_name;
DROP INDEX IF EXISTS idx_contacts_user_cpf;

CREATE INDEX IF NOT EXISTS idx_contacts_owner_name ON contacts (owner_id, lower(name));
CREATE INDEX IF NOT EXISTS idx_contacts_owner_cpf ON contacts (owner_id, cpf);

-- Garante a FK correta
ALTER TABLE contacts DROP CONSTRAINT IF EXISTS fk_contact_user;
ALTER TABLE contacts
  ADD CONSTRAINT fk_contact_user FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
