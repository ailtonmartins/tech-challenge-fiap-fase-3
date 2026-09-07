package br.com.fiap.techchallenge.notificacao.service.impl;

import br.com.fiap.techchallenge.notificacao.service.NotificacaoService;
import br.com.fiap.techchallenge.notificacao.dto.LembreteConsulta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoEmailService implements NotificacaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoEmailService.class);

    @Override
    public void enviarNotificacao(LembreteConsulta lembrete, String email) {
        LOGGER.info("Lembrete por e-mail preparado: eventId={}, consultaId={}, profissional={}, especialidade={}, dataHora={}",
                lembrete.eventId(), lembrete.consultaId(), lembrete.medico(), lembrete.especialidade(), lembrete.dataHoraFormatada());
    }
}
