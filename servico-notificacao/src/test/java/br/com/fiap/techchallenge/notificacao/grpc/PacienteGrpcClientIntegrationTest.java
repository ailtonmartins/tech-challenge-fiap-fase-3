package br.com.fiap.techchallenge.notificacao.grpc;

import br.com.fiap.techchallenge.contratos.paciente.v1.BuscaPacienteByIdGrpc;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.contratos.paciente.v1.ObterDadosDoPacienteRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PacienteGrpcClientIntegrationTest {

    private Server grpcServer;
    private PacienteGrpcClient client;

    @BeforeEach
    void setUp() throws IOException {
        grpcServer = ServerBuilder.forPort(0)
                .addService(new BuscaPacienteByIdGrpc.BuscaPacienteByIdImplBase() {
                    @Override
                    public void obterDadosDoPaciente(
                            ObterDadosDoPacienteRequest request,
                            StreamObserver<DadosDoPacienteResponse> responseObserver) {
                        responseObserver.onNext(DadosDoPacienteResponse.newBuilder()
                                .setPacienteId(request.getPacienteId())
                                .setPacienteNome("Maria da Silva")
                                .setPacienteEmail("maria@example.test")
                                .setPacienteTelefone("+5511999999999")
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
        client = new PacienteGrpcClient("127.0.0.1", grpcServer.getPort(), 1_000);
        client.init();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        client.shutdown();
        grpcServer.shutdownNow();
    }

    @Test
    void deveConsultarDadosAtuaisDoPacienteNoServidorGrpc() {
        String pacienteId = UUID.randomUUID().toString();

        DadosDoPacienteResponse response = client.obterDadosDoPaciente(pacienteId);

        assertThat(response.getPacienteId()).isEqualTo(pacienteId);
        assertThat(response.getPacienteNome()).isEqualTo("Maria da Silva");
        assertThat(response.getPacienteEmail()).isEqualTo("maria@example.test");
        assertThat(response.getPacienteTelefone()).isEqualTo("+5511999999999");
    }
}
