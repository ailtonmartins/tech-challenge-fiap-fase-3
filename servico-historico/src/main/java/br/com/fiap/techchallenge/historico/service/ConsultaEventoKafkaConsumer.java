package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.dto.ConsultaEvento;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsultaEventoKafkaConsumer {

    private final HistoricoProjectionService historicoProjectionService;

    public ConsultaEventoKafkaConsumer(HistoricoProjectionService historicoProjectionService) {
        this.historicoProjectionService = historicoProjectionService;
    }

    @KafkaListener(
            topics = {"consulta.criada.v1", "consulta.atualizada.v1"},
            groupId = "historico-consumer-v1"
    )
    public void consumir(ConsultaEvento evento) {
        historicoProjectionService.materializar(evento);
    }
}
