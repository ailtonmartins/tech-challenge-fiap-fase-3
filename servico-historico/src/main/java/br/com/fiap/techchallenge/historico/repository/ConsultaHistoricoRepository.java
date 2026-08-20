package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ConsultaHistoricoRepository extends JpaRepository<ConsultaHistorico, UUID> {

    Page<ConsultaHistorico> findByPacienteId(UUID pacienteId, Pageable pageable);

    Page<ConsultaHistorico> findByPacienteIdAndDataHoraAfter(UUID pacienteId, OffsetDateTime dataHora, Pageable pageable);
}
