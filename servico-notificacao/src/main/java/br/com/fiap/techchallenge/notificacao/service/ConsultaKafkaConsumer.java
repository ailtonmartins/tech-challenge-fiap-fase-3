package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.notificacao.exception.ConsultaNaoEncontradaException;
import br.com.fiap.techchallenge.notificacao.exception.ConsultaNaoNotificadaException;
import br.com.fiap.techchallenge.notificacao.exception.PacienteNaoEncontradoException;
import br.com.fiap.techchallenge.notificacao.model.Consulta;
import br.com.fiap.techchallenge.notificacao.model.Paciente;
import br.com.fiap.techchallenge.notificacao.model.StatusConsulta;
import br.com.fiap.techchallenge.notificacao.repository.ConsultaRepository;
import br.com.fiap.techchallenge.notificacao.repository.PacienteRepository;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoEmailService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoSmsService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoWhatsappService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class ConsultaKafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaKafkaConsumer.class);

    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoWhatsappService notificacaoWhatsappService;
    private final NotificacaoSmsService notificacaoSmsService;
    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;

    public ConsultaKafkaConsumer(NotificacaoEmailService notificacaoEmailService,
                                 NotificacaoWhatsappService notificacaoWhatsappService,
                                 NotificacaoSmsService notificacaoSmsService,
                                 ConsultaRepository consultaRepository,
                                 PacienteRepository pacienteRepository) {
        this.notificacaoEmailService = notificacaoEmailService;
        this.notificacaoWhatsappService = notificacaoWhatsappService;
        this.notificacaoSmsService = notificacaoSmsService;
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @KafkaListener(
            topics = "consulta.criada.v1",
            groupId = "notificacao_criacao_consulta")
    public void consumirEventoConsultaCriada(ConsultaCriadaEvento evento) {
        LOGGER.info("Evento de consulta criada consumido: {}", evento);
        Consulta consulta = consultaRepository.findById(evento.consultaId()).orElseThrow(() -> new ConsultaNaoEncontradaException(evento.consultaId()));
        if(!consulta.getStatus().equals(StatusConsulta.AGENDADA)) {
            LOGGER.info("Status de consulta inválido para notificação: {}", consulta.getStatus());
            throw new ConsultaNaoNotificadaException(consulta.getId());
        }
        Paciente paciente = pacienteRepository.findById(consulta.getPacienteId()).orElseThrow(() -> new PacienteNaoEncontradoException(consulta.getPacienteId()));
        notificarCanais(paciente);
        atualizarStatusConsulta(consulta);
    }

    private void atualizarStatusConsulta(Consulta consulta) {
        consulta.atualizarStatus(StatusConsulta.NOTIFICADA);
        consultaRepository.save(consulta);
    }

    private void notificarCanais(Paciente paciente) {
        notificacaoEmailService.enviarNotificacao(paciente.getNome(), paciente.getEmail());
        notificacaoSmsService.enviarNotificacao(paciente.getNome(), paciente.getTelefone());
        notificacaoWhatsappService.enviarNotificacao(paciente.getNome(), paciente.getTelefone());
    }

}
