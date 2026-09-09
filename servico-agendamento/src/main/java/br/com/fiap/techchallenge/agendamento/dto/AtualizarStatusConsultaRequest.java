package br.com.fiap.techchallenge.agendamento.dto;

import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AtualizarStatusConsultaRequest(
        @NotNull(message = "status é obrigatório")
        StatusConsulta status,

        @NotNull(message = "version é obrigatória")
        @PositiveOrZero(message = "version deve ser maior ou igual a zero")
        Long version) {
}
