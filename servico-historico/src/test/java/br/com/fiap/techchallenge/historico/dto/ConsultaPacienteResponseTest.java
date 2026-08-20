package br.com.fiap.techchallenge.historico.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultaPacienteResponseTest {

    @Test
    void naoDeveExporIdentificadorDoPaciente() {
        assertThat(ConsultaPacienteResponse.class.getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("pacienteId");
    }
}
