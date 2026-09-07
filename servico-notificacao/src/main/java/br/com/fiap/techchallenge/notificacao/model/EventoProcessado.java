package br.com.fiap.techchallenge.notificacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento_processado")
public class EventoProcessado {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "consulta_id", nullable = false, updatable = false)
    private UUID consultaId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private OffsetDateTime processedAt;

    protected EventoProcessado() {
    }

    public EventoProcessado(UUID eventId, UUID consultaId, String eventType) {
        this.eventId = eventId;
        this.consultaId = consultaId;
        this.eventType = eventType;
        this.processedAt = OffsetDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public String getEventType() {
        return eventType;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }
}
