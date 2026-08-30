package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsultaKafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaKafkaConsumer.class);

    private final NotificacaoOrquestradorService notificacaoOrquestradorService;

    public ConsultaKafkaConsumer(NotificacaoOrquestradorService notificacaoOrquestradorService) {
        this.notificacaoOrquestradorService = notificacaoOrquestradorService;
    }

    @KafkaListener(
            topics = "consulta.criada.v1",
            groupId = "notificacao_criacao_consulta")
    public void consumirEventoConsultaCriada(ConsultaCriadaEvento evento) {
        LOGGER.info("Evento de consulta criada consumido: {}", evento);
        notificacaoOrquestradorService.processarEventoConsultaCriada(evento);
    }
}
