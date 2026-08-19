CREATE TABLE evento_processado (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
