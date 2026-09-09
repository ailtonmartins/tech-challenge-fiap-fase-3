package br.com.fiap.techchallenge.agendamento.grpc;

import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import br.com.fiap.techchallenge.contratos.paciente.v1.DadosDoPacienteResponse;
import br.com.fiap.techchallenge.contratos.paciente.v1.ObterDadosDoPacienteRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BuscaPacienteGrpcServiceImplTest {

    private final PacienteRepository pacienteRepository = mock(PacienteRepository.class);
    private final BuscaPacienteGrpcServiceImpl service = new BuscaPacienteGrpcServiceImpl(pacienteRepository);

    @Test
    void deveRetornarInvalidArgumentParaIdentificadorMalformado() {
        StreamObserver<DadosDoPacienteResponse> responseObserver = mock(StreamObserver.class);

        service.obterDadosDoPaciente(request("identificador-invalido"), responseObserver);

        assertGrpcError(responseObserver, Status.Code.INVALID_ARGUMENT);
        verifyNoInteractions(pacienteRepository);
    }

    @Test
    void deveRetornarNotFoundParaPacienteInexistente() {
        StreamObserver<DadosDoPacienteResponse> responseObserver = mock(StreamObserver.class);
        UUID pacienteId = UUID.randomUUID();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        service.obterDadosDoPaciente(request(pacienteId.toString()), responseObserver);

        assertGrpcError(responseObserver, Status.Code.NOT_FOUND);
    }

    @Test
    void deveRetornarSomenteDadosNecessariosDoPaciente() {
        StreamObserver<DadosDoPacienteResponse> responseObserver = mock(StreamObserver.class);
        Paciente paciente = new Paciente("Maria da Silva", "maria@example.test", "+5511999999999", LocalDate.of(1990, 1, 1));
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));

        service.obterDadosDoPaciente(request(paciente.getId().toString()), responseObserver);

        ArgumentCaptor<DadosDoPacienteResponse> response = ArgumentCaptor.forClass(DadosDoPacienteResponse.class);
        verify(responseObserver).onNext(response.capture());
        verify(responseObserver).onCompleted();
        assertThat(response.getValue().getPacienteId()).isEqualTo(paciente.getId().toString());
        assertThat(response.getValue().getPacienteNome()).isEqualTo("Maria da Silva");
        assertThat(response.getValue().getPacienteEmail()).isEqualTo("maria@example.test");
        assertThat(response.getValue().getPacienteTelefone()).isEqualTo("+5511999999999");
    }

    private ObterDadosDoPacienteRequest request(String pacienteId) {
        return ObterDadosDoPacienteRequest.newBuilder().setPacienteId(pacienteId).build();
    }

    private void assertGrpcError(StreamObserver<DadosDoPacienteResponse> responseObserver, Status.Code expectedCode) {
        ArgumentCaptor<Throwable> error = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(error.capture());
        assertThat(Status.fromThrowable(error.getValue()).getCode()).isEqualTo(expectedCode);
    }
}
