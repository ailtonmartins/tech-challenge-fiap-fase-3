package br.com.fiap.techchallenge.historico.dto;

import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;

import java.util.UUID;

/** Dados de consulta que podem ser visualizados pelo próprio paciente. */
public record ConsultaPacienteResponse(
        UUID consultaId,
        String medico,
        String especialidade,
        String dataHora,
        String status) {

    public static ConsultaPacienteResponse from(ConsultaHistorico consulta) {
        return new ConsultaPacienteResponse(
                consulta.getConsultaId(), consulta.getMedico(), consulta.getEspecialidade(),
                consulta.getDataHora().toString(), consulta.getStatus().name());
    }
}
