package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.dto.CriarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.event.ConsultaCriadaEvent;
import br.com.fiap.techchallenge.agendamento.exception.ConflitoDeAgendamentoException;
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

    private String normalizarObservacoes(String observacoes) {
        return observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }
}
