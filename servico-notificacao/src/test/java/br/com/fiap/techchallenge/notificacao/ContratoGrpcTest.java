package br.com.fiap.techchallenge.notificacao;

import br.com.fiap.techchallenge.contratos.paciente.v1.BuscaPacienteByIdGrpc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContratoGrpcTest {

    @Test
    void deveDisponibilizarOStubGeradoDoContratoGrpc() {
        assertNotNull(BuscaPacienteByIdGrpc.getServiceDescriptor());
    }
}
