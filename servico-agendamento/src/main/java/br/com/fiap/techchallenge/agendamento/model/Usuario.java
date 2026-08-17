package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private PerfilUsuario role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "paciente_id")
    private UUID pacienteId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Usuario() {
    }

    public Usuario(String username, String passwordHash, PerfilUsuario role, boolean enabled, UUID pacienteId) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.pacienteId = pacienteId;
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

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public PerfilUsuario getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
