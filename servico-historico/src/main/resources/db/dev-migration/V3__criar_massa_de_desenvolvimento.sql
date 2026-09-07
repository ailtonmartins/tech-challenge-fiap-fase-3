INSERT INTO consulta_historico (
    consulta_id, paciente_id, paciente_nome, paciente_email, medico,
    especialidade, data_hora, status, event_occurred_at, updated_at
)
VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Maria Souza', 'maria@fiap.dev', 'Dra. Ana Martins', 'Cardiologia', CURRENT_TIMESTAMP + INTERVAL '7 days', 'AGENDADA', CURRENT_TIMESTAMP - INTERVAL '10 minutes', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Maria Souza', 'maria@fiap.dev', 'Dr. Carlos Lima', 'Clínica Geral', CURRENT_TIMESTAMP - INTERVAL '7 days', 'REALIZADA', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 'João Oliveira', 'joao@fiap.dev', 'Dra. Beatriz Costa', 'Ortopedia', CURRENT_TIMESTAMP + INTERVAL '14 days', 'CONFIRMADA', CURRENT_TIMESTAMP - INTERVAL '5 minutes', CURRENT_TIMESTAMP)
ON CONFLICT (consulta_id) DO NOTHING;

INSERT INTO evento_processado (event_id, processed_at)
VALUES
    ('40000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
    ('40000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    ('40000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP - INTERVAL '5 minutes')
ON CONFLICT (event_id) DO NOTHING;
