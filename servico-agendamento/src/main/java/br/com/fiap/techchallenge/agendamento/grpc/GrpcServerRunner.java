package br.com.fiap.techchallenge.agendamento.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerRunner {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);
    private Server server;

    @PostConstruct
    public void start() throws IOException {
        int port = 6565;
        server = ServerBuilder.forPort(port)
                .addService((BindableService) new BuscaPacienteGrpcServiceImpl())
                .build()
                .start();
        log.info("gRPC server started on port {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown, stopping gRPC server");
            GrpcServerRunner.this.stop();
        }));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC server stopped");
        }
    }
}
