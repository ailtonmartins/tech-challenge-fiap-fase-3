package br.com.fiap.techchallenge.agendamento.grpc.utils;

import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;

public class EntityMapperUtil {

    public static DadosDoPacienteResponse toResponse(Paciente paciente) {
        return DadosDoPacienteResponse.newBuilder()
                .setPacienteId(String.valueOf(paciente.getId()))
                .setPacienteNome(paciente.getNome())
                .setPacienteEmail(paciente.getEmail())
                .setPacienteTelefone(paciente.getTelefone())
                .build();
    }
}
