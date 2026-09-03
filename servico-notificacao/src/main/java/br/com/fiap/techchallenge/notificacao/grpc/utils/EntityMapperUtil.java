package br.com.fiap.techchallenge.notificacao.grpc.utils;

import br.com.fiap.techchallenge.notificacao.dto.DadosNotificacaoPaciente;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;


public class EntityMapperUtil {

    public static DadosNotificacaoPaciente toEntity(DadosDoPacienteResponse response) {
        return new DadosNotificacaoPaciente(
                response.getPacienteId(),
                response.getPacienteNome(),
                response.getPacienteEmail(),
                response.getPacienteTelefone()
        );
    }

}
