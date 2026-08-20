package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.dto.PaginaHistorico;
import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ConsultaHistoricoQueryService {

    private final ConsultaHistoricoRepository repository;
    private final int tamanhoMaximo;

    public ConsultaHistoricoQueryService(
            ConsultaHistoricoRepository repository,
            @Value("${app.historico.paginacao.tamanho-maximo}") int tamanhoMaximo) {
        this.repository = repository;
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

    private PageRequest pageable(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > tamanhoMaximo) {
            throw new IllegalArgumentException("Paginação inválida");
        }
        return PageRequest.of(pagina, tamanho, Sort.by("dataHora").descending());
    }
}
