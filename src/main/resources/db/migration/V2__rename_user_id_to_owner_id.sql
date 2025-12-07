ALTER TABLE contacts DROP CONSTRAINT IF EXISTS contacts_user_cpf_unique;
ALTER TABLE contacts ADD CONSTRAINT uk_user_cpf UNIQUE (owner_id, cpf);

DROP INDEX IF EXISTS idx_contacts_user_name;
DROP INDEX IF EXISTS idx_contacts_user_cpf;

CREATE INDEX IF NOT EXISTS idx_contacts_owner_name ON contacts (owner_id, lower(name));
CREATE INDEX IF NOT EXISTS idx_contacts_owner_cpf ON contacts (owner_id, cpf);

ALTER TABLE contacts DROP CONSTRAINT IF EXISTS fk_contact_user;
ALTER TABLE contacts
  ADD CONSTRAINT fk_contact_user FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;
