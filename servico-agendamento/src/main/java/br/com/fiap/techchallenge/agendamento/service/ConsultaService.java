package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.AtualizarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaAtualizadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.dto.CriarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.event.ConsultaCriadaEvent;
import br.com.fiap.techchallenge.agendamento.event.ConsultaAtualizadaEvent;
import br.com.fiap.techchallenge.agendamento.exception.ConflitoDeAgendamentoException;
import br.com.fiap.techchallenge.agendamento.exception.ConflitoDeAtualizacaoException;
import br.com.fiap.techchallenge.agendamento.exception.ConsultaNaoEncontradaException;
import br.com.fiap.techchallenge.agendamento.exception.ConsultaNaoPodeSerAlteradaException;
import br.com.fiap.techchallenge.agendamento.exception.PacienteNaoEncontradoException;
import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaAuthorizationService authorizationService;
    private final ApplicationEventPublisher eventPublisher;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ConsultaAuthorizationService authorizationService,
            ApplicationEventPublisher eventPublisher) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.authorizationService = authorizationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ConsultaResponse criar(CriarConsultaRequest request, Authentication authentication) {
        authorizationService.autorizarGerenciamentoDeConsulta(authentication);

        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new PacienteNaoEncontradoException(request.pacienteId()));

        if (consultaRepository.existsByPacienteIdAndDataHora(request.pacienteId(), request.dataHora())) {
            throw new ConflitoDeAgendamentoException();
        }

        Consulta consulta = consultaRepository.save(new Consulta(
                request.pacienteId(),
                request.medico().trim(),
                request.especialidade().trim(),
                request.dataHora(),
                normalizarObservacoes(request.observacoes())));

        eventPublisher.publishEvent(new ConsultaCriadaEvent(criarEvento(consulta, paciente)));
        return ConsultaResponse.from(consulta);
    }

    @Transactional
    public ConsultaResponse atualizar(UUID consultaId, AtualizarConsultaRequest request, Authentication authentication) {
        authorizationService.autorizarGerenciamentoDeConsulta(authentication);

        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ConsultaNaoEncontradaException(consultaId));
        validarConsultaPodeSerAlterada(consulta);

        if (!Objects.equals(consulta.getVersion(), request.version())) {
            throw new ConflitoDeAtualizacaoException();
        }
        if (consultaRepository.existsByPacienteIdAndDataHoraAndIdNot(
                consulta.getPacienteId(), request.dataHora(), consulta.getId())) {
            throw new ConflitoDeAgendamentoException();
        }

        Paciente paciente = pacienteRepository.findById(consulta.getPacienteId())
                .orElseThrow(() -> new PacienteNaoEncontradoException(consulta.getPacienteId()));
        consulta.atualizar(
                request.medico().trim(),
                request.especialidade().trim(),
                request.dataHora(),
                normalizarObservacoes(request.observacoes()));
        Consulta consultaAtualizada = consultaRepository.saveAndFlush(consulta);

        eventPublisher.publishEvent(new ConsultaAtualizadaEvent(criarEventoAtualizado(consultaAtualizada, paciente)));
        return ConsultaResponse.from(consultaAtualizada);
    }

    private ConsultaCriadaEvento criarEvento(Consulta consulta, Paciente paciente) {
        return new ConsultaCriadaEvento(
                UUID.randomUUID(),
                "CONSULTA_CRIADA",
                1,
                OffsetDateTime.now(),
                consulta.getId(),
                consulta.getPacienteId(),
                paciente.getNome(),
                paciente.getEmail(),
                consulta.getMedico(),
                consulta.getEspecialidade(),
                consulta.getDataHora(),
                consulta.getStatus().name());
    }

    private ConsultaAtualizadaEvento criarEventoAtualizado(Consulta consulta, Paciente paciente) {
        return new ConsultaAtualizadaEvento(
                UUID.randomUUID(),
                "CONSULTA_ATUALIZADA",
                1,
                OffsetDateTime.now(),
                consulta.getId(),
                consulta.getPacienteId(),
                paciente.getNome(),
                paciente.getEmail(),
                consulta.getMedico(),
                consulta.getEspecialidade(),
                consulta.getDataHora(),
                consulta.getStatus().name());
    }

    private void validarConsultaPodeSerAlterada(Consulta consulta) {
        if (consulta.getStatus() == br.com.fiap.techchallenge.agendamento.model.StatusConsulta.CANCELADA
                || consulta.getStatus() == br.com.fiap.techchallenge.agendamento.model.StatusConsulta.REALIZADA) {
            throw new ConsultaNaoPodeSerAlteradaException();
        }
    }

    private String normalizarObservacoes(String observacoes) {
        return observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }
}
