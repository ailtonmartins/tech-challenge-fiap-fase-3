package br.com.fiap.techchallenge.agendamento.dto;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultaResponse(
        UUID id,
        UUID pacienteId,
        String medico,
        String especialidade,
        OffsetDateTime dataHora,
        StatusConsulta status) {

    public static ConsultaResponse from(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPacienteId(),
                consulta.getMedico(),
                consulta.getEspecialidade(),
                consulta.getDataHora(),
                consulta.getStatus());
    }
}
