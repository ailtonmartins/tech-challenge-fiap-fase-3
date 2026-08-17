package br.com.fiap.techchallenge.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record AtualizarConsultaRequest(
        @NotBlank(message = "medico é obrigatório")
        @Size(max = 150, message = "medico deve ter no máximo 150 caracteres")
        String medico,

        @NotBlank(message = "especialidade é obrigatória")
        @Size(max = 100, message = "especialidade deve ter no máximo 100 caracteres")
        String especialidade,

        @NotNull(message = "dataHora é obrigatória")
        @Future(message = "dataHora deve estar no futuro")
        OffsetDateTime dataHora,

        @Size(max = 1000, message = "observacoes deve ter no máximo 1000 caracteres")
        String observacoes,

        @NotNull(message = "version é obrigatória")
        @PositiveOrZero(message = "version deve ser maior ou igual a zero")
        Long version) {
}
