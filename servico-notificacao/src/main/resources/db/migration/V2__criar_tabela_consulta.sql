CREATE TABLE consulta (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES paciente(id),
    medico VARCHAR(150) NOT NULL,
    especialidade VARCHAR(100) NOT NULL,
    data_hora TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacoes VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_consulta_status CHECK (status IN ('AGENDADA', 'CONFIRMADA', 'REALIZADA', 'NOTIFICADA','CANCELADA')),
    CONSTRAINT uk_consulta_paciente_data_hora UNIQUE (paciente_id, data_hora)
);
