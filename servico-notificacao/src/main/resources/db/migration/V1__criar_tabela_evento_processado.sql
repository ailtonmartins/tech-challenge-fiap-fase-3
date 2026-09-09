CREATE TABLE evento_processado (
    event_id UUID PRIMARY KEY,
    consulta_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_evento_processado_consulta_id ON evento_processado (consulta_id);
