package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.dto.ConsultaEvento;
import br.com.fiap.techchallenge.historico.model.ConsultaHistorico;
import br.com.fiap.techchallenge.historico.model.EventoProcessado;
import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import br.com.fiap.techchallenge.historico.repository.EventoProcessadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricoProjectionService {

    private final ConsultaHistoricoRepository consultaHistoricoRepository;
    private final EventoProcessadoRepository eventoProcessadoRepository;

    public HistoricoProjectionService(
            ConsultaHistoricoRepository consultaHistoricoRepository,
            EventoProcessadoRepository eventoProcessadoRepository) {
        this.consultaHistoricoRepository = consultaHistoricoRepository;
        this.eventoProcessadoRepository = eventoProcessadoRepository;
    }

    @Transactional
    public void materializar(ConsultaEvento evento) {
        if (eventoProcessadoRepository.existsById(evento.eventId())) {
            return;
        }

        consultaHistoricoRepository.findById(evento.consultaId())
                .ifPresentOrElse(
                        historico -> atualizarSeEventoForRecente(historico, evento),
                        () -> consultaHistoricoRepository.save(new ConsultaHistorico(evento)));

        eventoProcessadoRepository.save(new EventoProcessado(evento.eventId()));
    }

    private void atualizarSeEventoForRecente(ConsultaHistorico historico, ConsultaEvento evento) {
        if (evento.occurredAt().isAfter(historico.getEventOccurredAt())) {
            historico.atualizar(evento);
            consultaHistoricoRepository.save(historico);
        }
    }
}
