package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.CadastrarPacienteRequest;
import br.com.fiap.techchallenge.agendamento.dto.PacienteResponse;
import br.com.fiap.techchallenge.agendamento.exception.PacienteEmailDuplicadoException;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaAuthorizationService authorizationService;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void deveCadastrarPacienteComEmailNormalizadoQuandoProfissionalAutorizado() {
        Authentication authentication = autenticacaoProfissional();
        CadastrarPacienteRequest request = request("  MARIA@EXAMPLE.COM  ");
        when(pacienteRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PacienteResponse response = pacienteService.cadastrar(request, authentication);

        assertEquals("maria@example.com", response.email());
        assertEquals("Maria Souza", response.nome());
        verify(authorizationService).autorizarGerenciamentoDeConsulta(authentication);
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    void deveImpedirCadastroComEmailJaExistente() {
        Authentication authentication = autenticacaoProfissional();
        when(pacienteRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        assertThrows(PacienteEmailDuplicadoException.class,
                () -> pacienteService.cadastrar(request("maria@example.com"), authentication));

        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveImpedirCadastroPorPaciente() {
        Authentication authentication = new TestingAuthenticationToken("paciente.maria", null, "ROLE_PACIENTE");
        doThrow(new AccessDeniedException("negado"))
                .when(authorizationService).autorizarGerenciamentoDeConsulta(authentication);

        assertThrows(AccessDeniedException.class,
                () -> pacienteService.cadastrar(request("maria@example.com"), authentication));

        verify(pacienteRepository, never()).existsByEmailIgnoreCase(any());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    private CadastrarPacienteRequest request(String email) {
        return new CadastrarPacienteRequest("Maria Souza", email, "+55 11 99999-9999", LocalDate.of(1990, 5, 20));
    }

    private Authentication autenticacaoProfissional() {
        return new TestingAuthenticationToken("medico.ana", null, "ROLE_MEDICO");
    }
}
