package br.com.fiap.techchallenge.agendamento.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestAccessDeniedHandlerTest {

    private final RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler();

    @Test
    void deveRetornar403SemExporDetalhesInternos() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("detalhe interno"));

        assertEquals(403, response.getStatus());
        assertFalse(response.getContentAsString().contains("detalhe interno"));
    }
}
