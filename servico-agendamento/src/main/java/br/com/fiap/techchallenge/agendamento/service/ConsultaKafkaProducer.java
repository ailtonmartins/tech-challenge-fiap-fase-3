package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.config.KafkaTopicConfig;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.event.ConsultaCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ConsultaKafkaProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaKafkaProducer.class);

    private final KafkaTemplate<String, ConsultaCriadaEvento> kafkaTemplate;

    public ConsultaKafkaProducer(KafkaTemplate<String, ConsultaCriadaEvento> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarConsultaCriada(ConsultaCriadaEvent event) {
        ConsultaCriadaEvento evento = event.evento();
        kafkaTemplate.send(KafkaTopicConfig.CONSULTA_CRIADA_TOPIC, evento.consultaId().toString(), evento)
                .whenComplete((resultado, erro) -> {
                    if (erro == null) {
                        LOGGER.info("Evento de consulta publicado: eventId={}, consultaId={}", evento.eventId(), evento.consultaId());
                    } else {
                        LOGGER.error("Falha ao publicar evento de consulta: eventId={}, consultaId={}",
                                evento.eventId(), evento.consultaId(), erro);
                    }
                });
    }
}
