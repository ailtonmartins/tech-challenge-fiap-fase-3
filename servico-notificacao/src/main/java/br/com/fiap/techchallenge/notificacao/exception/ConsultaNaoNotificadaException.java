package br.com.fiap.techchallenge.notificacao.exception;

import java.util.UUID;

public class ConsultaNaoNotificadaException extends RuntimeException {

    public ConsultaNaoNotificadaException(UUID consultaId) {
        super("Consulta não notificada: " + consultaId);
    }
}
