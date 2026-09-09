package br.com.fiap.techchallenge.notificacao.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaEvento(
        UUID eventId,
        String eventType,
        int eventVersion,
        OffsetDateTime occurredAt,
        UUID consultaId,
        UUID pacienteId,
        String medico,
        String especialidade,
        OffsetDateTime dataHora,
        String status) {
}
