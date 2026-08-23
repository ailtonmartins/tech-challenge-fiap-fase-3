package br.com.fiap.techchallenge.notificacao.repository;

import br.com.fiap.techchallenge.notificacao.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

}
