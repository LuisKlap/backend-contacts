-- Altera a coluna neighborhood para permitir valores nulos
ALTER TABLE contacts ALTER COLUMN neighborhood DROP NOT NULL;
