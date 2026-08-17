package br.com.fiap.techchallenge.agendamento.exception;

public class PacienteEmailDuplicadoException extends RuntimeException {

    public PacienteEmailDuplicadoException() {
        super("Já existe um paciente cadastrado com este e-mail");
    }
}
