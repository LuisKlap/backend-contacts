-- =====================
-- USERS
-- =====================
CREATE TABLE IF NOT EXISTS users (
id BIGSERIAL PRIMARY KEY,
full_name VARCHAR(150) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================
-- CONTACTS
-- =====================
CREATE TABLE IF NOT EXISTS contacts (
id BIGSERIAL PRIMARY KEY,
owner_id BIGINT NOT NULL,

name VARCHAR(150) NOT NULL,
cpf CHAR(11) NOT NULL,
phone VARCHAR(30) NOT NULL,

cep VARCHAR(9) NOT NULL,

street VARCHAR(200) NOT NULL,
number VARCHAR(30) NOT NULL,
neighborhood VARCHAR(100) NOT NULL,
city VARCHAR(100) NOT NULL,
state CHAR(2) NOT NULL,
complement VARCHAR(150),

latitude DOUBLE PRECISION NOT NULL,
longitude DOUBLE PRECISION NOT NULL,

created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

CONSTRAINT fk_contact_user FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
CONSTRAINT uk_user_cpf UNIQUE (owner_id, cpf),
CONSTRAINT cpf_digits_only CHECK (cpf ~ '^[0-9]{11}$'),
CONSTRAINT state_two_letters CHECK (state ~ '^[A-Z]{2}$')
);

CREATE INDEX IF NOT EXISTS idx_contacts_owner_name ON contacts (owner_id, lower(name));
CREATE INDEX IF NOT EXISTS idx_contacts_owner_cpf ON contacts (owner_id, cpf);

-- =====================
-- Trigger para updated_at
-- =====================
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_contacts
BEFORE UPDATE ON contacts
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
