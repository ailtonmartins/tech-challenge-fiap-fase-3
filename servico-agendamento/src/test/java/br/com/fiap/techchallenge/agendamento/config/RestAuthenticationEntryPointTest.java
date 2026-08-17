package br.com.fiap.techchallenge.agendamento.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestAuthenticationEntryPointTest {

    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

    @Test
    void deveRetornar401SemExporDetalhesDaCredencial() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("senha incorreta"));

        assertEquals(401, response.getStatus());
        assertEquals("Basic realm=\"hospital\"", response.getHeader("WWW-Authenticate"));
        assertFalse(response.getContentAsString().contains("senha incorreta"));
    }
}
