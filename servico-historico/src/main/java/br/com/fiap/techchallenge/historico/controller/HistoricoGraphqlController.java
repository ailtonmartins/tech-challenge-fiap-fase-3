package br.com.fiap.techchallenge.historico.controller;

import br.com.fiap.techchallenge.historico.dto.PaginaHistorico;
import br.com.fiap.techchallenge.historico.dto.PaginaMinhasConsultas;
import br.com.fiap.techchallenge.historico.service.ConsultaHistoricoQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class HistoricoGraphqlController {

    private final ConsultaHistoricoQueryService queryService;

    public HistoricoGraphqlController(ConsultaHistoricoQueryService queryService) {
        this.queryService = queryService;
    }

    @QueryMapping
    public PaginaHistorico historicoDoPaciente(
            @Argument UUID pacienteId,
            @Argument Integer pagina,
            @Argument Integer tamanho) {
        return queryService.historicoDoPaciente(pacienteId, pagina == null ? 0 : pagina, tamanho == null ? 20 : tamanho);
    }

    @QueryMapping
    public PaginaHistorico consultasFuturas(
            @Argument UUID pacienteId,
            @Argument Integer pagina,
            @Argument Integer tamanho) {
        return queryService.consultasFuturas(pacienteId, pagina == null ? 0 : pagina, tamanho == null ? 20 : tamanho);
    }

    @QueryMapping
    public PaginaMinhasConsultas minhasConsultas(
            @Argument Boolean somenteFuturas,
            @Argument Integer pagina,
            @Argument Integer tamanho) {
        return queryService.minhasConsultas(
                Boolean.TRUE.equals(somenteFuturas), pagina == null ? 0 : pagina, tamanho == null ? 20 : tamanho);
    }
}
