package br.com.fiap.techchallenge.historico.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaMinhasConsultas(
        List<ConsultaPacienteResponse> consultas,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas) {

    public static PaginaMinhasConsultas from(Page<br.com.fiap.techchallenge.historico.model.ConsultaHistorico> pagina) {
        return new PaginaMinhasConsultas(
                pagina.map(ConsultaPacienteResponse::from).getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
