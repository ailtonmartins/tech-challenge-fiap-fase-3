package br.com.fiap.techchallenge.agendamento.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaCriadaEvento(
        UUID eventId,
        String eventType,
        int eventVersion,
        OffsetDateTime occurredAt,
        UUID consultaId,
        UUID pacienteId,
        String pacienteNome,
        String pacienteEmail,
        String medico,
        String especialidade,
        OffsetDateTime dataHora,
        String status) {
}
