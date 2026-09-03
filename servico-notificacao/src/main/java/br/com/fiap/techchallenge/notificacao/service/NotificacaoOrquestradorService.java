package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.notificacao.dto.DadosNotificacaoPaciente;
import br.com.fiap.techchallenge.notificacao.exception.ConsultaNaoEncontradaException;
import br.com.fiap.techchallenge.notificacao.exception.ConsultaNaoNotificadaException;
import br.com.fiap.techchallenge.notificacao.grpc.utils.EntityMapperUtil;
import br.com.fiap.techchallenge.notificacao.model.Consulta;
import br.com.fiap.techchallenge.notificacao.model.StatusConsulta;
import br.com.fiap.techchallenge.notificacao.repository.ConsultaRepository;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoEmailService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoSmsService;
import br.com.fiap.techchallenge.notificacao.service.impl.NotificacaoWhatsappService;
import br.com.fiap.techchallenge.notificacao.grpc.PacienteGrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class NotificacaoOrquestradorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoOrquestradorService.class);

    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoWhatsappService notificacaoWhatsappService;
    private final NotificacaoSmsService notificacaoSmsService;
    private final ConsultaRepository consultaRepository;
    private final PacienteGrpcClient pacienteGrpcClient;

    public NotificacaoOrquestradorService(NotificacaoEmailService notificacaoEmailService,
                                          NotificacaoWhatsappService notificacaoWhatsappService,
                                          NotificacaoSmsService notificacaoSmsService,
                                          ConsultaRepository consultaRepository,
                                          PacienteGrpcClient pacienteGrpcClient) {
        this.notificacaoEmailService = notificacaoEmailService;
        this.notificacaoWhatsappService = notificacaoWhatsappService;
        this.notificacaoSmsService = notificacaoSmsService;
        this.consultaRepository = consultaRepository;
        this.pacienteGrpcClient = pacienteGrpcClient;
    }

    public void processarEventoConsultaCriada(ConsultaCriadaEvento evento) {
        Consulta consulta = consultaRepository.findById(evento.consultaId()).orElseThrow(() -> new ConsultaNaoEncontradaException(evento.consultaId()));
        if (!consulta.getStatus().equals(StatusConsulta.AGENDADA)) {
            LOGGER.info("Status de consulta inválido para notificação: {}", consulta.getStatus());
            throw new ConsultaNaoNotificadaException(consulta.getId());
        }

        DadosDoPacienteResponse paciente = pacienteGrpcClient.obterDadosDoPaciente(String.valueOf(evento.pacienteId()));
        notificarCanais(EntityMapperUtil.toEntity(paciente));
        atualizarStatusConsulta(consulta);
    }

    private void atualizarStatusConsulta(Consulta consulta) {
        consulta.atualizarStatus(StatusConsulta.NOTIFICADA);
        consultaRepository.save(consulta);
    }

    private void notificarCanais(DadosNotificacaoPaciente paciente) {
        notificacaoEmailService.enviarNotificacao(paciente.nome(), paciente.email());
        notificacaoSmsService.enviarNotificacao(paciente.nome(), paciente.telefone());
        notificacaoWhatsappService.enviarNotificacao(paciente.nome(), paciente.telefone());
    }

}
