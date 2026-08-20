package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;

@ExtendWith(MockitoExtension.class)
class ConsultaHistoricoQueryServiceTest {

    @Mock
    private ConsultaHistoricoRepository repository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRejeitarTamanhoAcimaDoLimite() {
        var service = new ConsultaHistoricoQueryService(repository, jdbcTemplate, 50);

        assertThatThrownBy(() -> service.historicoDoPaciente(UUID.randomUUID(), 0, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Paginação inválida");
    }

    @Test
    void deveBuscarSomenteConsultasComDataFutura() {
        var service = new ConsultaHistoricoQueryService(repository, jdbcTemplate, 50);
        UUID pacienteId = UUID.randomUUID();
        when(repository.findByPacienteIdAndDataHoraAfter(org.mockito.ArgumentMatchers.eq(pacienteId),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(empty());

        service.consultasFuturas(pacienteId, 1, 10);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByPacienteIdAndDataHoraAfter(
                org.mockito.ArgumentMatchers.eq(pacienteId),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class), pageable.capture());
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void deveConsultarApenasOPacienteVinculadoAoUsuarioAutenticado() {
        var service = new ConsultaHistoricoQueryService(repository, jdbcTemplate, 50);
        UUID pacienteVinculado = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria.demo", "n/a",
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PACIENTE"))));
        when(jdbcTemplate.query(org.mockito.ArgumentMatchers.contains("FROM agendamento.usuario"),
                org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.ResultSetExtractor.class),
                org.mockito.ArgumentMatchers.eq("maria.demo"))).thenReturn(pacienteVinculado);
        when(repository.findByPacienteId(org.mockito.ArgumentMatchers.eq(pacienteVinculado),
                org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(empty());

        service.minhasConsultas(false, 0, 20);

        verify(repository).findByPacienteId(org.mockito.ArgumentMatchers.eq(pacienteVinculado),
                org.mockito.ArgumentMatchers.any(Pageable.class));
        SecurityContextHolder.clearContext();
    }
}
