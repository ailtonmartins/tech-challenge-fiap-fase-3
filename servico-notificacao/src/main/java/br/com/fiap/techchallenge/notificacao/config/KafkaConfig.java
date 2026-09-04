package br.com.fiap.techchallenge.notificacao.config;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "br.com.fiap.techchallenge.notificacao");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento");
        return props;
    }

    @Bean
    public ConsumerFactory<String, ConsultaCriadaEvento> consumerFactory() {
        JsonDeserializer<ConsultaCriadaEvento> deserializer = new JsonDeserializer<>(ConsultaCriadaEvento.class, false);
        deserializer.addTrustedPackages("br.com.fiap.techchallenge.notificacao");
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConsultaCriadaEvento> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ConsultaCriadaEvento> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
