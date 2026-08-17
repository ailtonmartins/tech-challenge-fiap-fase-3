package br.com.fiap.techchallenge.agendamento.exception;

public class ConflitoDeAtualizacaoException extends RuntimeException {

    public ConflitoDeAtualizacaoException() {
        super("A consulta foi alterada por outro usuário");
    }
}
