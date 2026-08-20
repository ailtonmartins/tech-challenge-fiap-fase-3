package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.dto.PaginaHistorico;
import br.com.fiap.techchallenge.historico.dto.PaginaMinhasConsultas;
import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ConsultaHistoricoQueryService {

    private final ConsultaHistoricoRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final int tamanhoMaximo;

    public ConsultaHistoricoQueryService(
            ConsultaHistoricoRepository repository,
            JdbcTemplate jdbcTemplate,
            @Value("${app.historico.paginacao.tamanho-maximo}") int tamanhoMaximo) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.tamanhoMaximo = tamanhoMaximo;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public PaginaHistorico historicoDoPaciente(UUID pacienteId, int pagina, int tamanho) {
        return PaginaHistorico.from(repository.findByPacienteId(pacienteId, pageable(pagina, tamanho)));
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public PaginaHistorico consultasFuturas(UUID pacienteId, int pagina, int tamanho) {
        return PaginaHistorico.from(repository.findByPacienteIdAndDataHoraAfter(
                pacienteId, OffsetDateTime.now(), pageable(pagina, tamanho)));
    }

    @PreAuthorize("hasRole('PACIENTE')")
    public PaginaMinhasConsultas minhasConsultas(boolean somenteFuturas, int pagina, int tamanho) {
        UUID pacienteId = pacienteIdDoUsuarioAutenticado();
        var pageable = pageable(pagina, tamanho);
        var consultas = somenteFuturas
                ? repository.findByPacienteIdAndDataHoraAfter(pacienteId, OffsetDateTime.now(), pageable)
                : repository.findByPacienteId(pacienteId, pageable);
        return PaginaMinhasConsultas.from(consultas);
    }

    private UUID pacienteIdDoUsuarioAutenticado() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AccessDeniedException("Acesso negado");
        }
        UUID pacienteId = jdbcTemplate.query(
                "SELECT paciente_id FROM agendamento.usuario "
                        + "WHERE username = ? AND role = 'PACIENTE' AND enabled = TRUE",
                resultSet -> resultSet.next() ? resultSet.getObject("paciente_id", UUID.class) : null,
                autenticacao.getName());
        if (pacienteId == null) {
            throw new AccessDeniedException("Acesso negado");
        }
        return pacienteId;
    }

    private PageRequest pageable(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > tamanhoMaximo) {
            throw new IllegalArgumentException("Paginação inválida");
        }
        return PageRequest.of(pagina, tamanho, Sort.by("dataHora").descending());
    }
}
