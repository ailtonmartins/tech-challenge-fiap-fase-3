CREATE TABLE paciente (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    data_nascimento DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uk_paciente_email_normalizado ON paciente (LOWER(email));
