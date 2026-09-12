package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.AtualizarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.AtualizarStatusConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaAtualizadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.dto.CriarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.event.ConsultaCriadaEvent;
import br.com.fiap.techchallenge.agendamento.event.ConsultaAtualizadaEvent;
import br.com.fiap.techchallenge.agendamento.exception.ConflitoDeAgendamentoException;
import br.com.fiap.techchallenge.agendamento.exception.ConflitoDeAtualizacaoException;
import br.com.fiap.techchallenge.agendamento.exception.ConsultaNaoPodeSerAlteradaException;
import br.com.fiap.techchallenge.agendamento.exception.PacienteNaoEncontradoException;
import br.com.fiap.techchallenge.agendamento.exception.StatusInvalidoException;
import br.com.fiap.techchallenge.agendamento.exception.TransicaoDeStatusInvalidaException;
import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaAuthorizationService authorizationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConsultaService consultaService;

    @Test
    void deveCriarConsultaAgendadaEPublicarEvento() {
        UUID pacienteId = UUID.randomUUID();
        CriarConsultaRequest request = request(pacienteId);
        Paciente paciente = new Paciente("Maria Souza", "maria@example.com", "+55 11 99999-9999", request.dataHora().toLocalDate().minusYears(30));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteIdAndDataHora(pacienteId, request.dataHora())).thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = consultaService.criar(request, authentication());

        assertEquals("AGENDADA", response.status().name());
        assertEquals(pacienteId, response.pacienteId());
        assertNotNull(response.id());

        ArgumentCaptor<ConsultaCriadaEvent> eventCaptor = ArgumentCaptor.forClass(ConsultaCriadaEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ConsultaCriadaEvento evento = eventCaptor.getValue().evento();
        assertEquals("CONSULTA_CRIADA", evento.eventType());
        assertEquals(response.id(), evento.consultaId());
        assertEquals("Maria Souza", evento.pacienteNome());
        assertEquals("AGENDADA", evento.status());
    }

    @Test
    void deveRejeitarConsultaParaPacienteInexistente() {
        UUID pacienteId = UUID.randomUUID();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        assertThrows(PacienteNaoEncontradoException.class,
                () -> consultaService.criar(request(pacienteId), authentication()));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarConflitoDeHorarioParaMesmoPaciente() {
        UUID pacienteId = UUID.randomUUID();
        CriarConsultaRequest request = request(pacienteId);
        Paciente paciente = new Paciente("Maria Souza", "maria@example.com", "+55 11 99999-9999", request.dataHora().toLocalDate().minusYears(30));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteIdAndDataHora(pacienteId, request.dataHora())).thenReturn(true);

        assertThrows(ConflitoDeAgendamentoException.class,
                () -> consultaService.criar(request, authentication()));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveAtualizarConsultaEPublicarEventoDeAtualizacao() {
        UUID pacienteId = UUID.randomUUID();
        Consulta consulta = consulta(pacienteId);
        AtualizarConsultaRequest request = atualizarRequest(consulta.getVersion());
        Paciente paciente = new Paciente("Maria Souza", "maria@example.com", "+55 11 99999-9999", request.dataHora().toLocalDate().minusYears(30));
        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByPacienteIdAndDataHoraAndIdNot(pacienteId, request.dataHora(), consulta.getId())).thenReturn(false);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(consultaRepository.saveAndFlush(consulta)).thenAnswer(invocation -> {
            ReflectionTestUtils.setField(consulta, "version", request.version() + 1);
            return consulta;
        });

        ConsultaResponse response = consultaService.atualizar(consulta.getId(), request, authentication());

        assertEquals("Neurologia", response.especialidade());
        assertEquals(request.dataHora(), response.dataHora());
        assertEquals(request.version() + 1, response.version());
        verify(consultaRepository).saveAndFlush(consulta);
        ArgumentCaptor<ConsultaAtualizadaEvent> eventCaptor = ArgumentCaptor.forClass(ConsultaAtualizadaEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ConsultaAtualizadaEvento evento = eventCaptor.getValue().evento();
        assertEquals("CONSULTA_ATUALIZADA", evento.eventType());
        assertEquals(consulta.getId(), evento.consultaId());
    }

    @Test
    void deveImpedirAtualizacaoComVersaoDesatualizada() {
        Consulta consulta = consulta(UUID.randomUUID());
        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));

        assertThrows(ConflitoDeAtualizacaoException.class,
                () -> consultaService.atualizar(consulta.getId(), atualizarRequest(consulta.getVersion() + 1), authentication()));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveImpedirAtualizacaoDeConsultaRealizadaOuCancelada() {
        Consulta consulta = consulta(UUID.randomUUID());
        ReflectionTestUtils.setField(consulta, "status", StatusConsulta.REALIZADA);
        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));

        assertThrows(ConsultaNaoPodeSerAlteradaException.class,
                () -> consultaService.atualizar(consulta.getId(), atualizarRequest(consulta.getVersion()), authentication()));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveConfirmarConsultaEPublicarEventoComNovoStatus() {
        UUID pacienteId = UUID.randomUUID();
        Consulta consulta = consulta(pacienteId);
        AtualizarStatusConsultaRequest request = new AtualizarStatusConsultaRequest(StatusConsulta.CONFIRMADA, consulta.getVersion());
        Paciente paciente = new Paciente("Maria Souza", "maria@example.com", "+55 11 99999-9999", OffsetDateTime.now().minusYears(30).toLocalDate());
        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(consultaRepository.saveAndFlush(consulta)).thenAnswer(invocation -> {
            ReflectionTestUtils.setField(consulta, "version", request.version() + 1);
            return consulta;
        });

        ConsultaResponse response = consultaService.atualizarStatus(consulta.getId(), request, authentication());

        assertEquals(StatusConsulta.CONFIRMADA, response.status());
        assertEquals(1L, response.version());
        ArgumentCaptor<ConsultaAtualizadaEvent> eventCaptor = ArgumentCaptor.forClass(ConsultaAtualizadaEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("CONFIRMADA", eventCaptor.getValue().evento().status());
    }

    @Test
    void deveImpedirTransicaoInvalidaOuReaberturaDeConsulta() {
        Consulta consulta = consulta(UUID.randomUUID());
        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));

        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> consultaService.atualizarStatus(consulta.getId(),
                        new AtualizarStatusConsultaRequest(StatusConsulta.REALIZADA, consulta.getVersion()), authentication()));

        ReflectionTestUtils.setField(consulta, "status", StatusConsulta.CANCELADA);
        assertThrows(TransicaoDeStatusInvalidaException.class,
                () -> consultaService.atualizarStatus(consulta.getId(),
                        new AtualizarStatusConsultaRequest(StatusConsulta.CONFIRMADA, consulta.getVersion()), authentication()));
        verify(consultaRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveConfirmarConsultaComStatusPermitido() {
        UUID pacienteId = UUID.randomUUID();
        Consulta consulta = consulta(pacienteId);
        Authentication auth = authentication();
        AtualizarStatusConsultaRequest request = new AtualizarStatusConsultaRequest(StatusConsulta.CANCELADA, consulta.getVersion());

        when(consultaRepository.findById(consulta.getId())).thenReturn(Optional.of(consulta));
        when(consultaRepository.saveAndFlush(consulta)).thenAnswer(invocation -> {
            ReflectionTestUtils.setField(consulta, "version", request.version() + 1);
            return consulta;
        });

        ConsultaResponse response = consultaService.confirmarConsulta(consulta.getId(), request, auth);

        assertEquals(StatusConsulta.CANCELADA, response.status());
        assertEquals(request.version() + 1, response.version());
        verify(authorizationService).confirmarOuCancelarConsulta(auth);
    }

    @Test
    void deveRejeitarStatusInvalidoNaConfirmacao() {
        Consulta consulta = consulta(UUID.randomUUID());
        Authentication auth = authentication();
        AtualizarStatusConsultaRequest request = new AtualizarStatusConsultaRequest(StatusConsulta.CONFIRMADA, consulta.getVersion());

        assertThrows(StatusInvalidoException.class,
                () -> consultaService.confirmarConsulta(consulta.getId(), request, auth));
        verify(authorizationService).confirmarOuCancelarConsulta(auth);
        verify(consultaRepository, never()).findById(any());
    }

    private CriarConsultaRequest request(UUID pacienteId) {
        return new CriarConsultaRequest(
                pacienteId,
                "Dra. Ana Silva",
                "Cardiologia",
                OffsetDateTime.now().plusDays(7),
                "Consulta de acompanhamento");
    }

    private Authentication authentication() {
        return org.mockito.Mockito.mock(Authentication.class);
    }

    private Consulta consulta(UUID pacienteId) {
        return new Consulta(
                pacienteId,
                "Dra. Ana Silva",
                "Cardiologia",
                OffsetDateTime.now().plusDays(7),
                "Consulta de acompanhamento");
    }

    private AtualizarConsultaRequest atualizarRequest(Long version) {
        return new AtualizarConsultaRequest(
                "Dra. Ana Silva",
                "Neurologia",
                OffsetDateTime.now().plusDays(10),
                "Horário reagendado",
                version);
    }
}
