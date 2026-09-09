package br.com.fiap.techchallenge.notificacao.repository;

import br.com.fiap.techchallenge.notificacao.model.NotificacaoResultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificacaoResultadoRepository extends JpaRepository<NotificacaoResultado, UUID> {
}
