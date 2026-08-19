package br.com.fiap.techchallenge.historico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento_processado")
public class EventoProcessado {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    protected EventoProcessado() {
    }

    public EventoProcessado(UUID eventId) {
        this.eventId = eventId;
    }

    @PrePersist
    void definirDataDeProcessamento() {
        processedAt = OffsetDateTime.now();
    }
}
