package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.EventoProcessado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoProcessadoRepository extends JpaRepository<EventoProcessado, UUID> {
}
