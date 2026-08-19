package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsultaHistoricoRepository extends JpaRepository<ConsultaHistorico, UUID> {
}
