package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.dto.ConsultaEvento;
import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;
import br.com.fiap.techchallenge.historico.model.EventoProcessado;
import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import br.com.fiap.techchallenge.historico.repository.EventoProcessadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoProjectionServiceTest {

    @Mock
    private ConsultaHistoricoRepository consultaHistoricoRepository;

    @Mock
    private EventoProcessadoRepository eventoProcessadoRepository;

    @InjectMocks
    private HistoricoProjectionService historicoProjectionService;

    private UUID consultaId;

    @BeforeEach
    void setUp() {
        consultaId = UUID.randomUUID();
    }

    @Test
    void deveCriarProjecaoERegistrarEventoQuandoAindaNaoProcessado() {
        ConsultaEvento evento = evento("CONSULTA_CRIADA", OffsetDateTime.parse("2026-08-17T10:00:00-03:00"));
        when(eventoProcessadoRepository.existsById(evento.eventId())).thenReturn(false);
        when(consultaHistoricoRepository.findById(consultaId)).thenReturn(Optional.empty());

        historicoProjectionService.materializar(evento);

        ArgumentCaptor<ConsultaHistorico> historico = ArgumentCaptor.forClass(ConsultaHistorico.class);
        verify(consultaHistoricoRepository).save(historico.capture());
        verify(eventoProcessadoRepository).save(any(EventoProcessado.class));
        assertThat(historico.getValue().getConsultaId()).isEqualTo(consultaId);
        assertThat(historico.getValue().getStatus()).hasToString("AGENDADA");
    }

    @Test
    void deveIgnorarEventoDuplicado() {
        ConsultaEvento evento = evento("CONSULTA_CRIADA", OffsetDateTime.parse("2026-08-17T10:00:00-03:00"));
        when(eventoProcessadoRepository.existsById(evento.eventId())).thenReturn(true);

        historicoProjectionService.materializar(evento);

        verifyNoInteractions(consultaHistoricoRepository);
        verify(eventoProcessadoRepository, never()).save(any(EventoProcessado.class));
    }

    @Test
    void deveIgnorarEventoAntigoMasMarcaLoComoProcessado() {
        ConsultaEvento eventoMaisNovo = evento("CONSULTA_ATUALIZADA", OffsetDateTime.parse("2026-08-17T11:00:00-03:00"));
        ConsultaEvento eventoAntigo = evento("CONSULTA_CRIADA", OffsetDateTime.parse("2026-08-17T10:00:00-03:00"));
        ConsultaHistorico historico = new ConsultaHistorico(eventoMaisNovo);
        when(eventoProcessadoRepository.existsById(eventoAntigo.eventId())).thenReturn(false);
        when(consultaHistoricoRepository.findById(consultaId)).thenReturn(Optional.of(historico));

        historicoProjectionService.materializar(eventoAntigo);

        verify(consultaHistoricoRepository, never()).save(any(ConsultaHistorico.class));
        verify(eventoProcessadoRepository).save(any(EventoProcessado.class));
        assertThat(historico.getEventOccurredAt()).isEqualTo(eventoMaisNovo.occurredAt());
    }

    @Test
    void deveAtualizarProjecaoQuandoEventoForMaisRecente() {
        ConsultaEvento eventoAntigo = evento("CONSULTA_CRIADA", OffsetDateTime.parse("2026-08-17T10:00:00-03:00"));
        ConsultaEvento eventoMaisNovo = evento("CONSULTA_ATUALIZADA", OffsetDateTime.parse("2026-08-17T11:00:00-03:00"));
        ConsultaHistorico historico = new ConsultaHistorico(eventoAntigo);
        when(eventoProcessadoRepository.existsById(eventoMaisNovo.eventId())).thenReturn(false);
        when(consultaHistoricoRepository.findById(consultaId)).thenReturn(Optional.of(historico));

        historicoProjectionService.materializar(eventoMaisNovo);

        verify(consultaHistoricoRepository).save(eq(historico));
        verify(eventoProcessadoRepository).save(any(EventoProcessado.class));
        assertThat(historico.getEventOccurredAt()).isEqualTo(eventoMaisNovo.occurredAt());
        assertThat(historico.getStatus()).hasToString("CONFIRMADA");
    }

    private ConsultaEvento evento(String tipo, OffsetDateTime ocorridoEm) {
        return new ConsultaEvento(
                UUID.randomUUID(),
                tipo,
                1,
                ocorridoEm,
                consultaId,
                UUID.randomUUID(),
                "Maria da Silva",
                "maria@exemplo.com",
                "Dra. Ana Silva",
                "Cardiologia",
                OffsetDateTime.parse("2026-09-10T14:00:00-03:00"),
                tipo.equals("CONSULTA_CRIADA") ? "AGENDADA" : "CONFIRMADA");
    }
}
