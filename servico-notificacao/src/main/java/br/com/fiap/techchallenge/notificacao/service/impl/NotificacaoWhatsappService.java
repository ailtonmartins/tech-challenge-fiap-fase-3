package br.com.fiap.techchallenge.notificacao.service.impl;

import br.com.fiap.techchallenge.notificacao.service.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoWhatsappService implements NotificacaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoWhatsappService.class);

    @Override
    public void enviarNotificacao(String nome, String whatsapp) {
       LOGGER.info("Enviando notificação para paciente {} referente à consulta médica no número de whatsapp {} ", nome, whatsapp);
    }

}
