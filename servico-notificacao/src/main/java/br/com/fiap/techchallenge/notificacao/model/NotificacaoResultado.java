package br.com.fiap.techchallenge.notificacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificacao_resultado")
public class NotificacaoResultado {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "consulta_id", nullable = false, updatable = false)
    private UUID consultaId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "registrado_em", nullable = false)
    private OffsetDateTime registradoEm;

    protected NotificacaoResultado() {
    }

    public NotificacaoResultado(UUID eventId, UUID consultaId, String status) {
        this.eventId = eventId;
        this.consultaId = consultaId;
        this.status = status;
        this.registradoEm = OffsetDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getRegistradoEm() {
        return registradoEm;
    }
}
