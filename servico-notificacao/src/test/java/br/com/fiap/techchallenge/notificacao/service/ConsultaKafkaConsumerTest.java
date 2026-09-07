package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.exception.EventoConsultaInvalidoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.annotation.KafkaListener;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ConsultaKafkaConsumerTest {

    private final NotificacaoOrquestradorService orquestrador = mock(NotificacaoOrquestradorService.class);
    private final ConsultaEventoValidator validator = new ConsultaEventoValidator();
    private final ConsultaKafkaConsumer consumer = new ConsultaKafkaConsumer(orquestrador, validator);

    @Test
    void deveEscutarTopicosDeCriacaoEAtualizacao() throws NoSuchMethodException {
        Method method = ConsultaKafkaConsumer.class.getDeclaredMethod("consumir", ConsultaEvento.class, String.class);
        KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);

        assertThat(kafkaListener).isNotNull();
        assertThat(Arrays.asList(kafkaListener.topics()))
                .containsExactlyInAnyOrder("consulta.criada.v1", "consulta.atualizada.v1");
    }

    @Test
    void deveIniciarProcessamentoParaEventoDeCriacao() {
        ConsultaEvento evento = evento("CONSULTA_CRIADA", 1);

        consumer.consumir(evento, "consulta.criada.v1");

        ArgumentCaptor<ConsultaEvento> captor = ArgumentCaptor.forClass(ConsultaEvento.class);
        verify(orquestrador).processarEvento(captor.capture());
        assertThat(captor.getValue()).isEqualTo(evento);
    }

    @Test
    void deveIniciarProcessamentoParaEventoDeAtualizacao() {
        ConsultaEvento evento = evento("CONSULTA_ATUALIZADA", 1);

        consumer.consumir(evento, "consulta.atualizada.v1");

        verify(orquestrador).processarEvento(evento);
    }

    @Test
    void naoDeveProcessarEventoInvalido() {
        ConsultaEvento evento = evento("CONSULTA_CRIADA", 2);

        assertThatThrownBy(() -> consumer.consumir(evento, "consulta.criada.v1"))
                .isInstanceOf(EventoConsultaInvalidoException.class);

        verifyNoInteractions(orquestrador);
    }

    @Test
    void devePropagarFalhaDoOrquestradorParaEvitarConfirmacaoDoOffset() {
        ConsultaEvento evento = evento("CONSULTA_CRIADA", 1);
        doThrow(new IllegalStateException("falha de processamento"))
                .when(orquestrador).processarEvento(evento);

        assertThatThrownBy(() -> consumer.consumir(evento, "consulta.criada.v1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha de processamento");
    }

    private ConsultaEvento evento(String tipo, int versao) {
        return new ConsultaEvento(
                UUID.randomUUID(),
                tipo,
                versao,
                OffsetDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Dra. Ana Martins",
                "Cardiologia",
                OffsetDateTime.now().plusDays(1),
                "AGENDADA");
    }
}
