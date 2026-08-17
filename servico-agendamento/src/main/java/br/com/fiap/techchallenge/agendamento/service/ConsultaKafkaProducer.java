package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.config.KafkaTopicConfig;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaAtualizadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.event.ConsultaAtualizadaEvent;
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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ConsultaKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarConsultaCriada(ConsultaCriadaEvent event) {
        ConsultaCriadaEvento evento = event.evento();
        publicar(
                KafkaTopicConfig.CONSULTA_CRIADA_TOPIC,
                evento.consultaId().toString(),
                evento,
                evento.eventId(),
                evento.consultaId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarConsultaAtualizada(ConsultaAtualizadaEvent event) {
        ConsultaAtualizadaEvento evento = event.evento();
        publicar(
                KafkaTopicConfig.CONSULTA_ATUALIZADA_TOPIC,
                evento.consultaId().toString(),
                evento,
                evento.eventId(),
                evento.consultaId()
        );
    }

    private void publicar(String topico, String chave, Object evento, java.util.UUID eventId, java.util.UUID consultaId) {
        kafkaTemplate.send(topico, chave, evento).whenComplete((resultado, erro) -> {
            if (erro == null) {
                LOGGER.info("Evento de consulta publicado: eventId={}, consultaId={}", eventId, consultaId);
            } else {
                LOGGER.error(
                        "Falha ao publicar evento de consulta: eventId={}, consultaId={}",
                        eventId,
                        consultaId,
                        erro
                );
            }
        });
    }
}
