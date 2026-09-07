package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.messaging.handler.annotation.Header;

@Service
public class ConsultaKafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaKafkaConsumer.class);

    private final NotificacaoOrquestradorService notificacaoOrquestradorService;
    private final ConsultaEventoValidator consultaEventoValidator;

    public ConsultaKafkaConsumer(
            NotificacaoOrquestradorService notificacaoOrquestradorService,
            ConsultaEventoValidator consultaEventoValidator) {
        this.notificacaoOrquestradorService = notificacaoOrquestradorService;
        this.consultaEventoValidator = consultaEventoValidator;
    }

    @KafkaListener(
            topics = {"consulta.criada.v1", "consulta.atualizada.v1"},
            groupId = "notificacao-consumer-v1",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumir(ConsultaEvento evento, @Header(KafkaHeaders.RECEIVED_TOPIC) String topico) {
        consultaEventoValidator.validar(evento, topico);
        LOGGER.info("Evento de consulta consumido: eventId={}, eventType={}, consultaId={}",
                evento.eventId(), evento.eventType(), evento.consultaId());
        notificacaoOrquestradorService.processarEvento(evento);
    }
}
