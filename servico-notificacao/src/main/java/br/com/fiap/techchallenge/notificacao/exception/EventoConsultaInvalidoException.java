package br.com.fiap.techchallenge.notificacao.exception;

public class EventoConsultaInvalidoException extends IllegalArgumentException {

    public EventoConsultaInvalidoException(String mensagem) {
        super(mensagem);
    }
}
