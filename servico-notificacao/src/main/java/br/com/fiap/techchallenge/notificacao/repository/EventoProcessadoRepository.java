package br.com.fiap.techchallenge.notificacao.repository;

import br.com.fiap.techchallenge.notificacao.model.EventoProcessado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoProcessadoRepository extends JpaRepository<EventoProcessado, UUID> {
}
