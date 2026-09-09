package br.com.fiap.techchallenge.notificacao.config;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigTest {

    @Test
    void deveUsarOBootstrapServerConfigurado() {
        var config = new KafkaConfig("kafka:29092");

        assertThat(config.consumerConfigs())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
    }

    @Test
    void naoDeveMisturarPropriedadesComAConfiguracaoDoJsonDeserializer() {
        var config = new KafkaConfig("kafka:29092");

        assertThat(config.consumerConfigs())
                .doesNotContainKeys(JsonDeserializer.TRUSTED_PACKAGES, JsonDeserializer.VALUE_DEFAULT_TYPE);
    }

    @Test
    void deveDesserializarContratoPublicadoPeloAgendamento() {
        JsonDeserializer<ConsultaEvento> deserializer = new JsonDeserializer<>(ConsultaEvento.class, false);
        deserializer.addTrustedPackages("br.com.fiap.techchallenge.notificacao");

        ConsultaEvento evento = deserializer.deserialize("consulta.criada.v1", ("""
                {
                  "eventId":"10000000-0000-0000-0000-000000000001",
                  "eventType":"CONSULTA_CRIADA",
                  "eventVersion":1,
                  "occurredAt":"2026-09-05T12:00:00-03:00",
                  "consultaId":"20000000-0000-0000-0000-000000000001",
                  "pacienteId":"30000000-0000-0000-0000-000000000001",
                  "pacienteNome":"Maria Souza",
                  "pacienteEmail":"maria@example.com",
                  "medico":"Dra. Ana Martins",
                  "especialidade":"Cardiologia",
                  "dataHora":"2026-09-10T14:00:00-03:00",
                  "status":"AGENDADA"
                }
                """).getBytes(StandardCharsets.UTF_8));

        assertThat(evento.eventType()).isEqualTo("CONSULTA_CRIADA");
        assertThat(evento.consultaId()).hasToString("20000000-0000-0000-0000-000000000001");
        assertThat(evento.pacienteId()).hasToString("30000000-0000-0000-0000-000000000001");
    }
}
