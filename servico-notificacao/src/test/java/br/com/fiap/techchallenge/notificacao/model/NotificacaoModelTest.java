package br.com.fiap.techchallenge.notificacao.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoModelTest {

    @Test
    void deveManterDadosEEstadoDaConsulta() {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime dataInicial = OffsetDateTime.now().plusDays(1);
        Consulta consulta = new Consulta(pacienteId, "Dra. Ana", "Cardiologia", dataInicial, "retorno");

        assertThat(consulta.getId()).isNotNull();
        assertThat(consulta.getPacienteId()).isEqualTo(pacienteId);
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(consulta.getVersion()).isZero();
        assertThat(consulta.setStatus(StatusConsulta.CONFIRMADA)).isEqualTo(StatusConsulta.CONFIRMADA);

        OffsetDateTime novaData = dataInicial.plusDays(1);
        consulta.atualizar("Dr. Bruno", "Neurologia", novaData, "novo retorno");
        consulta.atualizarStatus(StatusConsulta.NOTIFICADA);

        assertThat(consulta.getMedico()).isEqualTo("Dr. Bruno");
        assertThat(consulta.getEspecialidade()).isEqualTo("Neurologia");
        assertThat(consulta.getDataHora()).isEqualTo(novaData);
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.NOTIFICADA);
    }

    @Test
    void deveManterDadosDoPacienteERegistrosDeAuditoria() {
        Paciente paciente = new Paciente("Maria", "maria@example.com", "+5511999999999", LocalDate.of(1990, 1, 1));
        paciente.preencherDatasDeCriacao();
        paciente.atualizarDataDeAlteracao();

        assertThat(paciente.getId()).isNotNull();
        assertThat(paciente.getNome()).isEqualTo("Maria");
        assertThat(paciente.getEmail()).isEqualTo("maria@example.com");
        assertThat(paciente.getTelefone()).isEqualTo("+5511999999999");
        assertThat(paciente.getDataNascimento()).isEqualTo(LocalDate.of(1990, 1, 1));

        UUID eventId = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        EventoProcessado evento = new EventoProcessado(eventId, consultaId, "CONSULTA_CRIADA");
        NotificacaoResultado resultado = new NotificacaoResultado(eventId, consultaId, "ENVIADA");

        assertThat(evento.getEventId()).isEqualTo(eventId);
        assertThat(evento.getConsultaId()).isEqualTo(consultaId);
        assertThat(evento.getEventType()).isEqualTo("CONSULTA_CRIADA");
        assertThat(evento.getProcessedAt()).isNotNull();
        assertThat(resultado.getEventId()).isEqualTo(eventId);
        assertThat(resultado.getConsultaId()).isEqualTo(consultaId);
        assertThat(resultado.getStatus()).isEqualTo("ENVIADA");
        assertThat(resultado.getRegistradoEm()).isNotNull();
        assertThat(StatusConsulta.valueOf("CANCELADA")).isEqualTo(StatusConsulta.CANCELADA);
    }
}
