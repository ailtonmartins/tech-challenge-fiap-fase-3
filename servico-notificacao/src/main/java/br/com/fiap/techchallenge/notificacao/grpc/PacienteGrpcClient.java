package br.com.fiap.techchallenge.notificacao.grpc;

import br.com.fiap.techchallenge.contratos.paciente.v1.BuscaPacienteByIdGrpc;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.contratos.paciente.v1.ObterDadosDoPacienteRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PacienteGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(PacienteGrpcClient.class);
    private ManagedChannel channel;
    private BuscaPacienteByIdGrpc.BuscaPacienteByIdBlockingStub blockingStub;

    private String host = "localhost";
    private int port = 6565;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = BuscaPacienteByIdGrpc.newBlockingStub(channel);
        log.info("gRPC client connected to {}:{}", host, port);
    }

    public DadosDoPacienteResponse obterDadosDoPaciente(String pacienteId) {
        ObterDadosDoPacienteRequest req = ObterDadosDoPacienteRequest.newBuilder()
                .setPacienteId(pacienteId)
                .build();
        return blockingStub.obterDadosDoPaciente(req);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
