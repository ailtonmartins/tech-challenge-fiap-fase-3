ALTER TABLE evento_processado
    ADD COLUMN consulta_id UUID,
    ADD COLUMN event_type VARCHAR(50);

CREATE INDEX idx_evento_processado_consulta_id ON evento_processado (consulta_id);
