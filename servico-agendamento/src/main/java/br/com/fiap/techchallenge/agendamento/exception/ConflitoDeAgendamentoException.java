package br.com.fiap.techchallenge.agendamento.exception;

public class ConflitoDeAgendamentoException extends RuntimeException {

    public ConflitoDeAgendamentoException() {
        super("Já existe uma consulta agendada para o paciente neste horário");
    }
}
