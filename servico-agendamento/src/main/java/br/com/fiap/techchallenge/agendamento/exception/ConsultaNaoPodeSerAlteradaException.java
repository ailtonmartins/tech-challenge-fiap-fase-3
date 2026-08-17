package br.com.fiap.techchallenge.agendamento.exception;

public class ConsultaNaoPodeSerAlteradaException extends RuntimeException {

    public ConsultaNaoPodeSerAlteradaException() {
        super("Consultas canceladas ou realizadas não podem ser alteradas");
    }
}
