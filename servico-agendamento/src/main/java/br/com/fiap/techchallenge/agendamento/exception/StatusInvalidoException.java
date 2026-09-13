package br.com.fiap.techchallenge.agendamento.exception;

public class StatusInvalidoException extends RuntimeException {
    public StatusInvalidoException() {
        super("Status inválido para confirmação ou cancelamento de consulta");
    }
}
