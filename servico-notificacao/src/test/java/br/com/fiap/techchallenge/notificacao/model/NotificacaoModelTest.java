package br.com.fiap.techchallenge.notificacao.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoModelTest {

    @Test
    void deveManterRegistrosDeAuditoriaDaNotificacao() {
        UUID eventId = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        EventoProcessado evento = new EventoProcessado(eventId, consultaId, "CONSULTA_CRIADA");
        NotificacaoResultado resultado = new NotificacaoResultado(eventId, consultaId, "ENVIADA");

        assertThat(evento.getEventId()).isEqualTo(eventId);
        assertThat(evento.getConsultaId()).isEqualTo(consultaId);
        assertThat(evento.getEventType()).isEqualTo("CONSULTA_CRIADA");
        assertThat(evento.getProcessedAt()).isNotNull();
        assertThat(resultado.getEventId()).isEqualTo(eventId);
        assertThat(resultado.getConsultaId()).isEqualTo(consultaId);
        assertThat(resultado.getStatus()).isEqualTo("ENVIADA");
        assertThat(resultado.getRegistradoEm()).isNotNull();
    }
}
