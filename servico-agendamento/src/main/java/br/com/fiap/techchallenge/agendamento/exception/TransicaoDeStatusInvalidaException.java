package br.com.fiap.techchallenge.agendamento.exception;

public class TransicaoDeStatusInvalidaException extends RuntimeException {
    public TransicaoDeStatusInvalidaException() {
        super("Transição de status da consulta não permitida");
    }
}
