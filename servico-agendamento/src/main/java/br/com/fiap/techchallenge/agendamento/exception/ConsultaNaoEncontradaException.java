package br.com.fiap.techchallenge.agendamento.exception;

import java.util.UUID;

public class ConsultaNaoEncontradaException extends RuntimeException {

    public ConsultaNaoEncontradaException(UUID consultaId) {
        super("Consulta não encontrada: " + consultaId);
    }
}
