package br.com.fiap.techchallenge.agendamento.repository;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    boolean existsByPacienteIdAndDataHora(UUID pacienteId, OffsetDateTime dataHora);

    boolean existsByPacienteIdAndDataHoraAndIdNot(UUID pacienteId, OffsetDateTime dataHora, UUID id);
}
