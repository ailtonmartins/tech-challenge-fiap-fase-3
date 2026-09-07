package br.com.fiap.techchallenge.notificacao.grpc;

import br.com.fiap.techchallenge.contratos.paciente.v1.BuscaPacienteByIdGrpc;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.contratos.paciente.v1.ObterDadosDoPacienteRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PacienteGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(PacienteGrpcClient.class);
    private ManagedChannel channel;
    private BuscaPacienteByIdGrpc.BuscaPacienteByIdBlockingStub blockingStub;
    private final String host;
    private final int port;
    private final long deadlineMillis;

    public PacienteGrpcClient(
            @Value("${app.grpc.paciente.host:localhost}") String host,
            @Value("${app.grpc.paciente.port:6565}") int port,
            @Value("${app.grpc.paciente.deadline-millis:2000}") long deadlineMillis) {
        this.host = host;
        this.port = port;
        this.deadlineMillis = deadlineMillis;
    }

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = BuscaPacienteByIdGrpc.newBlockingStub(channel);
        log.info("gRPC client configured for {}:{} with deadline {} ms", host, port, deadlineMillis);
    }

    public DadosDoPacienteResponse obterDadosDoPaciente(String pacienteId) {
        ObterDadosDoPacienteRequest req = ObterDadosDoPacienteRequest.newBuilder()
                .setPacienteId(pacienteId)
                .build();
        return blockingStub
                .withDeadlineAfter(deadlineMillis, TimeUnit.MILLISECONDS)
                .obterDadosDoPaciente(req);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
