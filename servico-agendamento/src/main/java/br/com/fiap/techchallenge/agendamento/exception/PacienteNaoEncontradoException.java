package br.com.fiap.techchallenge.agendamento.exception;

import java.util.UUID;

public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException(UUID pacienteId) {
        super("Paciente não encontrado: " + pacienteId);
    }
}
