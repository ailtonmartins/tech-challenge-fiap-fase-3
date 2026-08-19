package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.config.KafkaTopicConfig;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaAtualizadaEvento;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaCriadaEvento;
import br.com.fiap.techchallenge.agendamento.event.ConsultaAtualizadaEvent;
import br.com.fiap.techchallenge.agendamento.event.ConsultaCriadaEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ConsultaKafkaProducer producer;

    @Test
    void devePublicarCriacaoComChaveDaConsultaEAguardarConfirmacaoDoBroker() {
        ConsultaCriadaEvento evento = eventoCriado();
        CompletableFuture<SendResult<String, Object>> confirmacao = new CompletableFuture<>();
        when(kafkaTemplate.send(KafkaTopicConfig.CONSULTA_CRIADA_TOPIC, evento.consultaId().toString(), evento))
                .thenReturn(confirmacao);

        producer.publicarConsultaCriada(new ConsultaCriadaEvent(evento));

        verify(kafkaTemplate).send(KafkaTopicConfig.CONSULTA_CRIADA_TOPIC, evento.consultaId().toString(), evento);
        confirmacao.complete(null);
    }

    @Test
    void devePublicarAtualizacaoComChaveDaConsulta() {
        ConsultaAtualizadaEvento evento = eventoAtualizado();
        CompletableFuture<SendResult<String, Object>> confirmacao = new CompletableFuture<>();
        when(kafkaTemplate.send(KafkaTopicConfig.CONSULTA_ATUALIZADA_TOPIC, evento.consultaId().toString(), evento))
                .thenReturn(confirmacao);

        producer.publicarConsultaAtualizada(new ConsultaAtualizadaEvent(evento));

        verify(kafkaTemplate).send(KafkaTopicConfig.CONSULTA_ATUALIZADA_TOPIC, evento.consultaId().toString(), evento);
        confirmacao.complete(null);
    }

    private ConsultaCriadaEvento eventoCriado() {
        return new ConsultaCriadaEvento(
                UUID.randomUUID(),
                "CONSULTA_CRIADA",
                1,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Maria Souza",
                "maria@example.com",
                "Dra. Ana Silva",
                "Cardiologia",
                OffsetDateTime.now().plusDays(1),
                "AGENDADA");
    }

    private ConsultaAtualizadaEvento eventoAtualizado() {
        ConsultaCriadaEvento criado = eventoCriado();
        return new ConsultaAtualizadaEvento(
                criado.eventId(),
                "CONSULTA_ATUALIZADA",
                criado.eventVersion(),
                criado.occurredAt(),
                criado.consultaId(),
                criado.pacienteId(),
                criado.pacienteNome(),
                criado.pacienteEmail(),
                criado.medico(),
                criado.especialidade(),
                criado.dataHora(),
                criado.status());
    }
}
