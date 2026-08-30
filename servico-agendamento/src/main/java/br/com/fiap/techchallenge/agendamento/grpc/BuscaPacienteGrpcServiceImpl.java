package br.com.fiap.techchallenge.agendamento.grpc;

import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import br.com.fiap.techchallenge.contratos.paciente.v1.BuscaPacienteByIdGrpc;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.contratos.paciente.v1.ObterDadosDoPacienteRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BuscaPacienteGrpcServiceImpl extends BuscaPacienteByIdGrpc.BuscaPacienteByIdImplBase {
    private static final Logger log = LoggerFactory.getLogger(BuscaPacienteGrpcServiceImpl.class);

    private PacienteRepository pacienteRepository;

    @Override
    public void obterDadosDoPaciente(ObterDadosDoPacienteRequest request, StreamObserver<DadosDoPacienteResponse> responseObserver) {
        log.info("Obtendo dados do paciente com id={}", request.getPacienteId());
        Paciente paciente = pacienteRepository.findById(UUID.fromString(request.getPacienteId())).orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        DadosDoPacienteResponse response = DadosDoPacienteResponse.newBuilder()
                .setPacienteId(paciente.getId().toString())
                .setPacienteNome(paciente.getNome())
                .setPacienteEmail(paciente.getEmail())
                .setPacienteTelefone(paciente.getTelefone())
                .build();

        responseObserver.onCompleted();
    }
}
