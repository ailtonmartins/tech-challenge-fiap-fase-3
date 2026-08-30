package br.com.fiap.techchallenge.notificacao.dto;

import java.util.UUID;

public record ConsultaCriadaEvento(
        UUID consultaId,
        UUID pacienteId) {
}
