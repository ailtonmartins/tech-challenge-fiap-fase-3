package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.dto.LembreteConsulta;
import br.com.fiap.techchallenge.notificacao.grpc.PacienteGrpcClient;
import br.com.fiap.techchallenge.notificacao.model.EventoProcessado;
import br.com.fiap.techchallenge.notificacao.repository.EventoProcessadoRepository;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoEmailService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoSmsService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoWhatsappService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificacaoOrquestradorServiceTest {

    private final NotificacaoEmailService emailService = mock(NotificacaoEmailService.class);
    private final NotificacaoWhatsappService whatsappService = mock(NotificacaoWhatsappService.class);
    private final NotificacaoSmsService smsService = mock(NotificacaoSmsService.class);
    private final EventoProcessadoRepository eventoProcessadoRepository = mock(EventoProcessadoRepository.class);
    private final PacienteGrpcClient pacienteGrpcClient = mock(PacienteGrpcClient.class);
    private final NotificacaoResultadoService notificacaoResultadoService = mock(NotificacaoResultadoService.class);
    private final NotificacaoOrquestradorService service = new NotificacaoOrquestradorService(
            emailService,
            whatsappService,
            smsService,
            eventoProcessadoRepository,
            pacienteGrpcClient,
            notificacaoResultadoService);

    @Test
    void deveProcessarEventoDiretamenteERegistrarRastreabilidade() {
        ConsultaEvento evento = evento();
        when(pacienteGrpcClient.obterDadosDoPaciente(evento.pacienteId().toString())).thenReturn(paciente());

        service.processarEvento(evento);

        ArgumentCaptor<LembreteConsulta> lembrete = ArgumentCaptor.forClass(LembreteConsulta.class);
        verify(emailService).enviarNotificacao(lembrete.capture(), org.mockito.ArgumentMatchers.eq("maria@example.com"));
        verify(smsService).enviarNotificacao(lembrete.getValue(), "+5511999999999");
        verify(whatsappService).enviarNotificacao(lembrete.getValue(), "+5511999999999");
        assertThat(lembrete.getValue().nomePaciente()).isEqualTo("Maria Souza");
        assertThat(lembrete.getValue().medico()).isEqualTo(evento.medico());
        assertThat(lembrete.getValue().especialidade()).isEqualTo(evento.especialidade());
        assertThat(lembrete.getValue().dataHoraFormatada()).contains("BRT");
        ArgumentCaptor<EventoProcessado> captor = ArgumentCaptor.forClass(EventoProcessado.class);
        verify(eventoProcessadoRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(evento.eventId());
        assertThat(captor.getValue().getConsultaId()).isEqualTo(evento.consultaId());
        assertThat(captor.getValue().getEventType()).isEqualTo(evento.eventType());
        verify(notificacaoResultadoService).registrar(evento, "ENVIADA");
    }

    @Test
    void naoDeveEnviarNovamenteQuandoEventoJaFoiProcessado() {
        ConsultaEvento evento = evento();
        when(eventoProcessadoRepository.existsById(evento.eventId())).thenReturn(true);

        service.processarEvento(evento);

        verifyNoInteractions(pacienteGrpcClient, emailService, smsService, whatsappService);
        verify(eventoProcessadoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void devePropagarFalhaAntesDeRegistrarEvento() {
        ConsultaEvento evento = evento();
        when(pacienteGrpcClient.obterDadosDoPaciente(evento.pacienteId().toString()))
                .thenThrow(new IllegalStateException("gRPC indisponível"));

        assertThatThrownBy(() -> service.processarEvento(evento))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("gRPC indisponível");

        verifyNoInteractions(emailService, smsService, whatsappService);
        verify(eventoProcessadoRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(notificacaoResultadoService).registrar(evento, "FALHA");
    }

    @Test
    void naoDeveEnviarConsultaPassadaOuCancelada() {
        ConsultaEvento evento = new ConsultaEvento(
                UUID.randomUUID(), "CONSULTA_ATUALIZADA", 1, OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(),
                "Dra. Ana Martins", "Cardiologia", OffsetDateTime.now().minusMinutes(1), "CANCELADA");

        service.processarEvento(evento);

        verifyNoInteractions(pacienteGrpcClient, emailService, smsService, whatsappService);
        verify(eventoProcessadoRepository).save(org.mockito.ArgumentMatchers.any(EventoProcessado.class));
        verify(notificacaoResultadoService).registrar(evento, "NAO_ENVIADA");
    }

    private ConsultaEvento evento() {
        return new ConsultaEvento(
                UUID.randomUUID(),
                "CONSULTA_CRIADA",
                1,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Dra. Ana Martins",
                "Cardiologia",
                OffsetDateTime.now().plusDays(1),
                "AGENDADA");
    }

    private DadosDoPacienteResponse paciente() {
        return DadosDoPacienteResponse.newBuilder()
                .setPacienteId(UUID.randomUUID().toString())
                .setPacienteNome("Maria Souza")
                .setPacienteEmail("maria@example.com")
                .setPacienteTelefone("+5511999999999")
                .build();
    }
}
