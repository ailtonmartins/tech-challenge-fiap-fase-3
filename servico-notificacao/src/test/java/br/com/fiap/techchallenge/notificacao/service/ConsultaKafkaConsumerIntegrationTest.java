package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = ConsultaKafkaConsumerIntegrationTest.KafkaIntegrationConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"consulta.criada.v1", "consulta.atualizada.v1"})
class ConsultaKafkaConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, ConsultaEvento> kafkaTemplate;

    @Autowired
    private NotificacaoOrquestradorService orquestrador;

    @Test
    void deveConsumirEventosDeCriacaoEAtualizacaoDoKafka() {
        kafkaTemplate.send("consulta.criada.v1", evento("CONSULTA_CRIADA")).join();
        kafkaTemplate.send("consulta.atualizada.v1", evento("CONSULTA_ATUALIZADA")).join();

        verify(orquestrador, timeout(10_000).times(2)).processarEvento(any(ConsultaEvento.class));
    }

    private ConsultaEvento evento(String eventType) {
        return new ConsultaEvento(UUID.randomUUID(), eventType, 1, OffsetDateTime.now(), UUID.randomUUID(), UUID.randomUUID(),
                "Dra. Ana Martins", "Cardiologia", OffsetDateTime.now().plusDays(1), "AGENDADA");
    }

    @Configuration
    @EnableKafka
    static class KafkaIntegrationConfig {

        @Bean
        NotificacaoOrquestradorService orquestrador() {
            return mock(NotificacaoOrquestradorService.class);
        }

        @Bean
        ConsultaKafkaConsumer consumer(NotificacaoOrquestradorService orquestrador) {
            return new ConsultaKafkaConsumer(orquestrador, new ConsultaEventoValidator());
        }

        @Bean
        ProducerFactory<String, ConsultaEvento> producerFactory(org.springframework.core.env.Environment environment) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("bootstrap.servers", environment.getProperty("spring.embedded.kafka.brokers"));
            properties.put("key.serializer", StringSerializer.class);
            properties.put("value.serializer", JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(properties);
        }

        @Bean
        KafkaTemplate<String, ConsultaEvento> kafkaTemplate(ProducerFactory<String, ConsultaEvento> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, ConsultaEvento> consumerFactory(org.springframework.core.env.Environment environment) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.embedded.kafka.brokers"));
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, "notificacao-integration-test");
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            JsonDeserializer<ConsultaEvento> deserializer = new JsonDeserializer<>(ConsultaEvento.class, false);
            deserializer.addTrustedPackages("br.com.fiap.techchallenge.notificacao");
            return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
        }

        @Bean(name = "kafkaListenerContainerFactory")
        ConcurrentKafkaListenerContainerFactory<String, ConsultaEvento> kafkaListenerContainerFactory(
                ConsumerFactory<String, ConsultaEvento> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, ConsultaEvento> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }
    }
}
