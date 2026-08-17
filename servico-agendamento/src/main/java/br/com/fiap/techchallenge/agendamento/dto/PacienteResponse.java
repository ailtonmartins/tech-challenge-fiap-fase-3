package br.com.fiap.techchallenge.agendamento.dto;

import br.com.fiap.techchallenge.agendamento.model.Paciente;

import java.time.LocalDate;
import java.util.UUID;

public record PacienteResponse(UUID id, String nome, String email, String telefone, LocalDate dataNascimento) {

    public static PacienteResponse from(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNome(),
                paciente.getEmail(),
                paciente.getTelefone(),
                paciente.getDataNascimento());
    }
}
