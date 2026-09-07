package br.com.fiap.techchallenge.historico.controller;

import br.com.fiap.techchallenge.historico.dto.ConsultaEvento;
import br.com.fiap.techchallenge.historico.service.ConsultaEventoKafkaConsumer;
import br.com.fiap.techchallenge.historico.service.ConsultaHistoricoQueryService;
import br.com.fiap.techchallenge.historico.service.HistoricoProjectionService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoricoGraphqlControllerTest {

    @Test
    void deveAplicarPaginacaoPadraoEDelegarConsultas() {
        ConsultaHistoricoQueryService service = mock(ConsultaHistoricoQueryService.class);
        HistoricoGraphqlController controller = new HistoricoGraphqlController(service);
        UUID pacienteId = UUID.randomUUID();

        controller.historicoDoPaciente(pacienteId, null, null);
        controller.consultasFuturas(pacienteId, 2, 10);
        controller.minhasConsultas(null, null, null);
        controller.minhasConsultas(true, 1, 5);

        verify(service).historicoDoPaciente(pacienteId, 0, 20);
        verify(service).consultasFuturas(pacienteId, 2, 10);
        verify(service).minhasConsultas(false, 0, 20);
        verify(service).minhasConsultas(true, 1, 5);
    }

    @Test
    void deveEncaminharEventoKafkaParaProjecao() {
        HistoricoProjectionService projectionService = mock(HistoricoProjectionService.class);
        ConsultaEventoKafkaConsumer consumer = new ConsultaEventoKafkaConsumer(projectionService);
        ConsultaEvento evento = new ConsultaEvento(UUID.randomUUID(), "CONSULTA_CRIADA", 1, OffsetDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(), "Maria", "maria@example.com", "Dra. Ana", "Cardiologia",
                OffsetDateTime.now().plusDays(1), "AGENDADA");

        consumer.consumir(evento);

        verify(projectionService).materializar(evento);
    }
}
