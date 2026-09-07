package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.LembreteConsulta;

public interface NotificacaoService {
    void enviarNotificacao(LembreteConsulta lembrete, String destino);
}
