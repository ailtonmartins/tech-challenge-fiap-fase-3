CREATE TABLE consulta_historico (
    consulta_id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    paciente_nome VARCHAR(150) NOT NULL,
    paciente_email VARCHAR(254) NOT NULL,
    medico VARCHAR(150) NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    data_hora TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    event_occurred_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_consulta_historico_paciente_data_hora ON consulta_historico (paciente_id, data_hora);
