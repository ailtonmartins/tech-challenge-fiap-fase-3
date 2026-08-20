package br.com.fiap.techchallenge.historico.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphqlExceptionHandlerTest {

    private final GraphqlExceptionHandler handler = new GraphqlExceptionHandler();

    @Test
    void deveExporCodigoDeValidacaoEstavel() {
        var erro = handler.entradaInvalida(new IllegalArgumentException("Paginação inválida"));

        assertThat(erro.getExtensions()).containsEntry("code", "VALIDATION_ERROR");
        assertThat(erro.getMessage()).isEqualTo("Paginação inválida");
    }

    @Test
    void deveExporCodigoDeAcessoNegadoEstavel() {
        var erro = handler.acessoNegado();

        assertThat(erro.getExtensions()).containsEntry("code", "FORBIDDEN");
    }
}
