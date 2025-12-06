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
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

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

    CONSTRAINT contacts_user_cpf_unique UNIQUE (user_id, cpf),
    CONSTRAINT cpf_digits_only CHECK (cpf ~ '^[0-9]{11}$'),
    CONSTRAINT state_two_letters CHECK (state ~ '^[A-Z]{2}$')
);

CREATE INDEX IF NOT EXISTS idx_contacts_user_name ON contacts (user_id, lower(name));
CREATE INDEX IF NOT EXISTS idx_contacts_user_cpf ON contacts (user_id, cpf);

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
