package br.com.fiap.techchallenge.notificacao.grpc.utils;

import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperUtilTest {

    @Test
    void deveMapearRespostaGrpcParaDadosDeNotificacao() {
        var grpcResponse = DadosDoPacienteResponse.newBuilder()
                .setPacienteId("paciente-1")
                .setPacienteNome("Maria")
                .setPacienteEmail("maria@example.com")
                .setPacienteTelefone("+5511999999999")
                .build();

        var paciente = EntityMapperUtil.toEntity(grpcResponse);

        assertThat(paciente.id()).isEqualTo("paciente-1");
        assertThat(paciente.nome()).isEqualTo("Maria");
        assertThat(paciente.email()).isEqualTo("maria@example.com");
        assertThat(paciente.telefone()).isEqualTo("+5511999999999");
    }
}
