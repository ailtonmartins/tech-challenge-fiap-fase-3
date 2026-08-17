package br.com.fiap.techchallenge.agendamento.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    public static final String CONSULTA_CRIADA_TOPIC = "consulta.criada.v1";

    @Bean
    NewTopic consultaCriadaTopic() {
        return new NewTopic(CONSULTA_CRIADA_TOPIC, 1, (short) 1);
    }
}
