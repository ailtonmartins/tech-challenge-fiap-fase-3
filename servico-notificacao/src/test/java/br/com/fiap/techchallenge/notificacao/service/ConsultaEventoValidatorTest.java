package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.exception.EventoConsultaInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsultaEventoValidatorTest {

    private final ConsultaEventoValidator validator = new ConsultaEventoValidator();

    @Test
    void deveAceitarEventoDeCriacaoValido() {
        assertThatCode(() -> validator.validar(evento("CONSULTA_CRIADA", 1), "consulta.criada.v1"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveAceitarEventoDeAtualizacaoValido() {
        assertThatCode(() -> validator.validar(evento("CONSULTA_ATUALIZADA", 1), "consulta.atualizada.v1"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarVersaoNaoSuportada() {
        assertThatThrownBy(() -> validator.validar(evento("CONSULTA_CRIADA", 2), "consulta.criada.v1"))
                .isInstanceOf(EventoConsultaInvalidoException.class)
                .hasMessageContaining("Versão de evento não suportada");
    }

    @Test
    void deveRejeitarTipoIncompativelComOTopico() {
        assertThatThrownBy(() -> validator.validar(evento("CONSULTA_ATUALIZADA", 1), "consulta.criada.v1"))
                .isInstanceOf(EventoConsultaInvalidoException.class)
                .hasMessageContaining("incompatível");
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
