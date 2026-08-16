package br.com.fiap.techchallenge.historico;

import br.com.fiap.techchallenge.contratos.historico.v1.HistoricoNotificacaoServiceGrpc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContratoGrpcTest {

    @Test
    void deveDisponibilizarOStubGeradoDoContratoGrpc() {
        assertNotNull(HistoricoNotificacaoServiceGrpc.getServiceDescriptor());
    }
}
