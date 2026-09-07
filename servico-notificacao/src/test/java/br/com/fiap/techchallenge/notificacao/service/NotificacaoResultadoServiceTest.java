package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.model.NotificacaoResultado;
import br.com.fiap.techchallenge.notificacao.repository.NotificacaoResultadoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacaoResultadoServiceTest {

    @Test
    void deveRegistrarResultadoComIdentificadoresDoEvento() {
        NotificacaoResultadoRepository repository = mock(NotificacaoResultadoRepository.class);
        NotificacaoResultadoService service = new NotificacaoResultadoService(repository);
        ConsultaEvento evento = new ConsultaEvento(UUID.randomUUID(), "CONSULTA_CRIADA", 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), "Dra. Ana", "Cardiologia", OffsetDateTime.now().plusDays(1), "AGENDADA");

        service.registrar(evento, "ENVIADA");

        ArgumentCaptor<NotificacaoResultado> resultado = ArgumentCaptor.forClass(NotificacaoResultado.class);
        verify(repository).save(resultado.capture());
        assertThat(resultado.getValue().getEventId()).isEqualTo(evento.eventId());
        assertThat(resultado.getValue().getConsultaId()).isEqualTo(evento.consultaId());
        assertThat(resultado.getValue().getStatus()).isEqualTo("ENVIADA");
        assertThat(resultado.getValue().getRegistradoEm()).isNotNull();
    }
}
