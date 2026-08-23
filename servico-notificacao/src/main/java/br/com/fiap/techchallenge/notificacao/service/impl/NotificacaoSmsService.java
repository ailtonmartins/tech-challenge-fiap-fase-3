package br.com.fiap.techchallenge.notificacao.service.impl;

import br.com.fiap.techchallenge.notificacao.service.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoSmsService implements NotificacaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoSmsService.class);

    @Override
    public void enviarNotificacao(String nome, String telefone) {
       LOGGER.info("Enviando notificação para paciente {} referente à consulta médica no número de telefone {} ", nome, telefone);
    }
}
