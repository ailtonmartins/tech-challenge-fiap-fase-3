package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.AtualizarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.AtualizarStatusConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.CadastrarPacienteRequest;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.dto.CriarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.PacienteResponse;
import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import br.com.fiap.techchallenge.agendamento.service.ConsultaService;
import br.com.fiap.techchallenge.agendamento.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgendamentoControllerTest {

    @Test
    void deveDelegarCadastroDePacienteERetornarCreated() {
        PacienteService service = mock(PacienteService.class);
        PacienteController controller = new PacienteController(service);
        var authentication = autenticacao("paciente", "ROLE_PACIENTE");
        var request = new CadastrarPacienteRequest("Maria", "maria@example.com", "+5511999999999", LocalDate.of(1990, 1, 1));
        var response = new PacienteResponse(UUID.randomUUID(), "Maria", "maria@example.com", "+5511999999999", request.dataNascimento());
        when(service.cadastrar(request, authentication)).thenReturn(response);

        var resultado = controller.cadastrar(request, authentication);

        assertThat(resultado.getStatusCode().value()).isEqualTo(201);
        assertThat(resultado.getBody()).isEqualTo(response);
        verify(service).cadastrar(request, authentication);
    }

    @Test
    void deveDelegarCriacaoEAtualizacaoDeConsulta() {
        ConsultaService service = mock(ConsultaService.class);
        ConsultaController controller = new ConsultaController(service);
        var authentication = autenticacao("medico", "ROLE_MEDICO");
        UUID pacienteId = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        OffsetDateTime dataHora = OffsetDateTime.now().plusDays(1);
        var criar = new CriarConsultaRequest(pacienteId, "Dra. Ana", "Cardiologia", dataHora, "retorno");
        var atualizar = new AtualizarConsultaRequest("Dra. Ana", "Cardiologia", dataHora.plusDays(1), "alterada", 0L);
        var atualizarStatus = new AtualizarStatusConsultaRequest(StatusConsulta.CONFIRMADA, 0L);
        var response = new ConsultaResponse(consultaId, pacienteId, "Dra. Ana", "Cardiologia", dataHora, StatusConsulta.AGENDADA, 0L);
        when(service.criar(criar, authentication)).thenReturn(response);
        when(service.atualizar(consultaId, atualizar, authentication)).thenReturn(response);
        when(service.atualizarStatus(consultaId, atualizarStatus, authentication)).thenReturn(response);

        assertThat(controller.criar(criar, authentication).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.atualizar(consultaId, atualizar, authentication)).isEqualTo(response);
        assertThat(controller.atualizarStatus(consultaId, atualizarStatus, authentication)).isEqualTo(response);
        verify(service).criar(criar, authentication);
        verify(service).atualizar(consultaId, atualizar, authentication);
        verify(service).atualizarStatus(consultaId, atualizarStatus, authentication);
    }

    @Test
    void deveDelegarConfirmacaoOuCancelamentoDeConsulta() {
        ConsultaService service = mock(ConsultaService.class);
        ConsultaController controller = new ConsultaController(service);
        var authentication = autenticacao("paciente", "ROLE_PACIENTE");
        UUID consultaId = UUID.randomUUID();
        var confirmar = new AtualizarStatusConsultaRequest(StatusConsulta.CANCELADA, 2L);
        var response = new ConsultaResponse(consultaId, UUID.randomUUID(), "Dra. Ana", "Cardiologia",
                OffsetDateTime.now().plusDays(2), StatusConsulta.CANCELADA, 3L);
        when(service.confirmarConsulta(consultaId, confirmar, authentication)).thenReturn(response);

        assertThat(controller.confirmarConsulta(consultaId, confirmar, authentication)).isEqualTo(response);
        verify(service).confirmarConsulta(consultaId, confirmar, authentication);
    }

    @Test
    void deveRetornarPerfilOuSemPerfilDoUsuarioAutenticado() {
        UsuarioController controller = new UsuarioController();

        assertThat(controller.usuarioAutenticado(autenticacao("ana", "ROLE_MEDICO")))
                .isEqualTo(new UsuarioController.UsuarioAutenticadoResponse("ana", "MEDICO"));
        assertThat(controller.usuarioAutenticado(new UsernamePasswordAuthenticationToken("sem-perfil", null)))
                .isEqualTo(new UsuarioController.UsuarioAutenticadoResponse("sem-perfil", "SEM_PERFIL"));
    }

    private UsernamePasswordAuthenticationToken autenticacao(String nome, String role) {
        return new UsernamePasswordAuthenticationToken(nome, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
