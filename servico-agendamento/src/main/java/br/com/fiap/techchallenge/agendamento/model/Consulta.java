package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(nullable = false)
    private String medico;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(length = 1000)
    private String observacoes;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Consulta() {
    }

    public Consulta(UUID pacienteId, String medico, String especialidade, OffsetDateTime dataHora, String observacoes) {
        this.id = UUID.randomUUID();
        this.pacienteId = pacienteId;
        this.medico = medico;
        this.especialidade = especialidade;
        this.dataHora = dataHora;
        this.status = StatusConsulta.AGENDADA;
        this.observacoes = observacoes;
    }

    @PrePersist
    void preencherDatasDeCriacao() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = agora;
        updatedAt = agora;
    }

    @PreUpdate
    void atualizarDataDeAlteracao() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public String getMedico() {
        return medico;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public StatusConsulta getStatus() {
        return status;
    }
}
