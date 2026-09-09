package br.com.fiap.techchallenge.notificacao.dto;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record LembreteConsulta(
        UUID eventId,
        UUID consultaId,
        String nomePaciente,
        String medico,
        String especialidade,
        OffsetDateTime dataHora) {

    private static final ZoneId ZONA_DO_LEMBRETE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm z");

    public String dataHoraFormatada() {
        return dataHora.atZoneSameInstant(ZONA_DO_LEMBRETE).format(FORMATO_DATA_HORA);
    }
}
