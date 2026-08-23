package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaCriadaEvento;

public interface NotificacaoService {
    public void enviarNotificacao(String nome, String canal);
}
