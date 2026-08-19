package br.com.fiap.techchallenge.historico.model;

import br.com.fiap.techchallenge.historico.dto.ConsultaEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consulta_historico")
public class ConsultaHistorico {

    @Id
    @Column(name = "consulta_id")
    private UUID consultaId;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "paciente_nome", nullable = false)
    private String pacienteNome;

    @Column(name = "paciente_email", nullable = false)
    private String pacienteEmail;

    @Column(nullable = false)
    private String medico;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(name = "event_occurred_at", nullable = false)
    private OffsetDateTime eventOccurredAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ConsultaHistorico() {
    }

    public ConsultaHistorico(ConsultaEvento evento) {
        atualizar(evento);
    }

    public void atualizar(ConsultaEvento evento) {
        this.consultaId = evento.consultaId();
        this.pacienteId = evento.pacienteId();
        this.pacienteNome = evento.pacienteNome();
        this.pacienteEmail = evento.pacienteEmail();
        this.medico = evento.medico();
        this.especialidade = evento.especialidade();
        this.dataHora = evento.dataHora();
        this.status = StatusConsulta.valueOf(evento.status());
        this.eventOccurredAt = evento.occurredAt();
        this.updatedAt = OffsetDateTime.now();
    }

    public OffsetDateTime getEventOccurredAt() {
        return eventOccurredAt;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public StatusConsulta getStatus() {
        return status;
    }
}
