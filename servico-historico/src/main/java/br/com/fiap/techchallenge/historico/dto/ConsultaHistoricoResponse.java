package br.com.fiap.techchallenge.historico.dto;

import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;

import java.util.UUID;

public record ConsultaHistoricoResponse(
        UUID consultaId,
        UUID pacienteId,
        String medico,
        String especialidade,
        String dataHora,
        String status) {

    public static ConsultaHistoricoResponse from(ConsultaHistorico consulta) {
        return new ConsultaHistoricoResponse(
                consulta.getConsultaId(), consulta.getPacienteId(), consulta.getMedico(),
                consulta.getEspecialidade(), consulta.getDataHora().toString(), consulta.getStatus().name());
    }
}
