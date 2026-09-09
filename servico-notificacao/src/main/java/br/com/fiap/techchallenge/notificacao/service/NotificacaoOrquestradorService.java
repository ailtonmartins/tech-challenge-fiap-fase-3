package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.dto.DadosNotificacaoPaciente;
import br.com.fiap.techchallenge.notificacao.dto.LembreteConsulta;
import br.com.fiap.techchallenge.notificacao.grpc.utils.EntityMapperUtil;
import br.com.fiap.techchallenge.notificacao.model.EventoProcessado;
import br.com.fiap.techchallenge.notificacao.repository.EventoProcessadoRepository;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoEmailService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoSmsService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoWhatsappService;
import br.com.fiap.techchallenge.notificacao.grpc.PacienteGrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class NotificacaoOrquestradorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoOrquestradorService.class);

    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoWhatsappService notificacaoWhatsappService;
    private final NotificacaoSmsService notificacaoSmsService;
    private final EventoProcessadoRepository eventoProcessadoRepository;
    private final PacienteGrpcClient pacienteGrpcClient;
    private final NotificacaoResultadoService notificacaoResultadoService;

    public NotificacaoOrquestradorService(NotificacaoEmailService notificacaoEmailService,
                                          NotificacaoWhatsappService notificacaoWhatsappService,
                                          NotificacaoSmsService notificacaoSmsService,
                                          EventoProcessadoRepository eventoProcessadoRepository,
                                          PacienteGrpcClient pacienteGrpcClient,
                                          NotificacaoResultadoService notificacaoResultadoService) {
        this.notificacaoEmailService = notificacaoEmailService;
        this.notificacaoWhatsappService = notificacaoWhatsappService;
        this.notificacaoSmsService = notificacaoSmsService;
        this.eventoProcessadoRepository = eventoProcessadoRepository;
        this.pacienteGrpcClient = pacienteGrpcClient;
        this.notificacaoResultadoService = notificacaoResultadoService;
    }

    @Transactional
    public void processarEvento(ConsultaEvento evento) {
        if (eventoProcessadoRepository.existsById(evento.eventId())) {
            LOGGER.info("Evento de notificação já processado: eventId={}, consultaId={}",
                    evento.eventId(), evento.consultaId());
            return;
        }

        if (!consultaPodeSerNotificada(evento)) {
            notificacaoResultadoService.registrar(evento, "NAO_ENVIADA");
            eventoProcessadoRepository.save(new EventoProcessado(
                    evento.eventId(), evento.consultaId(), evento.eventType()));
            LOGGER.info("Notificação não enviada: eventId={}, consultaId={}, statusConsulta={}",
                    evento.eventId(), evento.consultaId(), evento.status());
            return;
        }

        try {
            DadosDoPacienteResponse paciente = pacienteGrpcClient.obterDadosDoPaciente(String.valueOf(evento.pacienteId()));
            notificarCanais(EntityMapperUtil.toEntity(paciente), evento);
            eventoProcessadoRepository.save(new EventoProcessado(
                    evento.eventId(), evento.consultaId(), evento.eventType()));
            notificacaoResultadoService.registrar(evento, "ENVIADA");
            LOGGER.info("Notificação processada: eventId={}, consultaId={}, eventType={}",
                    evento.eventId(), evento.consultaId(), evento.eventType());
        } catch (RuntimeException exception) {
            notificacaoResultadoService.registrar(evento, "FALHA");
            LOGGER.warn("Falha ao preparar notificação: eventId={}, consultaId={}",
                    evento.eventId(), evento.consultaId());
            throw exception;
        }
    }

    private boolean consultaPodeSerNotificada(ConsultaEvento evento) {
        return evento.dataHora().isAfter(java.time.OffsetDateTime.now())
                && ("AGENDADA".equals(evento.status()) || "CONFIRMADA".equals(evento.status()));
    }

    private void notificarCanais(DadosNotificacaoPaciente paciente, ConsultaEvento evento) {
        LembreteConsulta lembrete = new LembreteConsulta(
                evento.eventId(), evento.consultaId(), paciente.nome(), evento.medico(), evento.especialidade(), evento.dataHora());
        notificacaoEmailService.enviarNotificacao(lembrete, paciente.email());
        notificacaoSmsService.enviarNotificacao(lembrete, paciente.telefone());
        notificacaoWhatsappService.enviarNotificacao(lembrete, paciente.telefone());
    }

}
