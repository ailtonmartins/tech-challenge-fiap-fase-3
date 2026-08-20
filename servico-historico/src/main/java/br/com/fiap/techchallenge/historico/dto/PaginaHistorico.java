package br.com.fiap.techchallenge.historico.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaHistorico(
        List<ConsultaHistoricoResponse> consultas,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas) {

    public static PaginaHistorico from(Page<br.com.fiap.techchallenge.historico.model.ConsultaHistorico> pagina) {
        return new PaginaHistorico(
                pagina.map(ConsultaHistoricoResponse::from).getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
