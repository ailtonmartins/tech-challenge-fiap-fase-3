package br.com.fiap.techchallenge.notificacao.service.impl;

import br.com.fiap.techchallenge.notificacao.dto.LembreteConsulta;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificacaoCanalServiceTest {

    @Test
    void devePrepararLembreteEmTodosOsCanais() {
        LembreteConsulta lembrete = new LembreteConsulta(UUID.randomUUID(), UUID.randomUUID(), "Maria", "Dra. Ana", "Cardiologia", OffsetDateTime.now().plusDays(1));

        assertThatCode(() -> new NotificacaoEmailService().enviarNotificacao(lembrete, "maria@example.com")).doesNotThrowAnyException();
        assertThatCode(() -> new NotificacaoSmsService().enviarNotificacao(lembrete, "+5511999999999")).doesNotThrowAnyException();
        assertThatCode(() -> new NotificacaoWhatsappService().enviarNotificacao(lembrete, "+5511999999999")).doesNotThrowAnyException();
    }
}
