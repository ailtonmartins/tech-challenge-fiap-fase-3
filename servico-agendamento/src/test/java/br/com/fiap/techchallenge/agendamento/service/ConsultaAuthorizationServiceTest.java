package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.model.PerfilUsuario;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaAuthorizationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConsultaAuthorizationService authorizationService;

    @Test
    void devePermitirQueMedicoOuEnfermeiroGerenciemConsulta() {
        assertDoesNotThrow(() -> authorizationService.autorizarGerenciamentoDeConsulta(autenticacao("medico.ana", "ROLE_MEDICO")));
        assertDoesNotThrow(() -> authorizationService.autorizarGerenciamentoDeConsulta(autenticacao("enfermeiro.carlos", "ROLE_ENFERMEIRO")));
    }

    @Test
    void deveImpedirQuePacienteGerencieConsulta() {
        assertThrows(AccessDeniedException.class,
                () -> authorizationService.autorizarGerenciamentoDeConsulta(autenticacao("paciente.maria", "ROLE_PACIENTE")));
    }

    @Test
    void devePermitirQuePacienteConfirmeOuCanceleConsulta() {
        UUID pacienteDaMaria = UUID.randomUUID();
        Usuario maria = new Usuario("paciente.maria", "hash", PerfilUsuario.PACIENTE, true, pacienteDaMaria);
        when(usuarioRepository.findByUsername("paciente.maria")).thenReturn(Optional.of(maria));

        assertDoesNotThrow(() -> authorizationService.confirmarOuCancelarConsulta(
                autenticacao("paciente.maria", "ROLE_PACIENTE"), pacienteDaMaria));
    }

    @Test
    void deveImpedirQuePacienteConfirmeOuCanceleConsultaDeOutroPaciente() {
        Usuario maria = new Usuario("paciente.maria", "hash", PerfilUsuario.PACIENTE, true, UUID.randomUUID());
        when(usuarioRepository.findByUsername("paciente.maria")).thenReturn(Optional.of(maria));

        assertThrows(AccessDeniedException.class, () -> authorizationService.confirmarOuCancelarConsulta(
                autenticacao("paciente.maria", "ROLE_PACIENTE"), UUID.randomUUID()));
    }

    @Test
    void deveImpedirQueMedicoOuEnfermeiroConfirmeOuCanceleConsulta() {
        assertThrows(AccessDeniedException.class,
                () -> authorizationService.confirmarOuCancelarConsulta(autenticacao("medico.ana", "ROLE_MEDICO"), UUID.randomUUID()));
        assertThrows(AccessDeniedException.class,
                () -> authorizationService.confirmarOuCancelarConsulta(autenticacao("enfermeiro.carlos", "ROLE_ENFERMEIRO"), UUID.randomUUID()));
    }

    @Test
    void devePermitirQueProfissionalConsulteHistoricoDeQualquerPaciente() {
        assertDoesNotThrow(() -> authorizationService.autorizarConsultaDeHistorico(
                autenticacao("medico.ana", "ROLE_MEDICO"), UUID.randomUUID()));
    }

    @Test
    void devePermitirQuePacienteConsulteSomenteOsPropriosDados() {
        UUID pacienteDaMaria = UUID.randomUUID();
        Usuario maria = new Usuario("paciente.maria", "hash", PerfilUsuario.PACIENTE, true, pacienteDaMaria);
        when(usuarioRepository.findByUsername("paciente.maria")).thenReturn(Optional.of(maria));

        assertDoesNotThrow(() -> authorizationService.autorizarConsultaDeHistorico(
                autenticacao("paciente.maria", "ROLE_PACIENTE"), pacienteDaMaria));
        assertThrows(AccessDeniedException.class, () -> authorizationService.autorizarConsultaDeHistorico(
                autenticacao("paciente.maria", "ROLE_PACIENTE"), UUID.randomUUID()));
    }

    private Authentication autenticacao(String username, String role) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(username, null, role);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
