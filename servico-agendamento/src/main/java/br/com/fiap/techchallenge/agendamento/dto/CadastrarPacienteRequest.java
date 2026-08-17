package br.com.fiap.techchallenge.agendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CadastrarPacienteRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 150, message = "nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email possui formato inválido")
        @Size(max = 254, message = "email deve ter no máximo 254 caracteres")
        String email,

        @NotBlank(message = "telefone é obrigatório")
        @Size(max = 30, message = "telefone deve ter no máximo 30 caracteres")
        String telefone,

        @NotNull(message = "dataNascimento é obrigatória")
        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento) {
}
