CREATE TABLE notificacao_resultado (
    event_id UUID PRIMARY KEY,
    consulta_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notificacao_resultado_consulta_id ON notificacao_resultado (consulta_id);
