package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.model.NotificacaoResultado;
import br.com.fiap.techchallenge.notificacao.repository.NotificacaoResultadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoResultadoService {

    private final NotificacaoResultadoRepository notificacaoResultadoRepository;

    public NotificacaoResultadoService(NotificacaoResultadoRepository notificacaoResultadoRepository) {
        this.notificacaoResultadoRepository = notificacaoResultadoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(ConsultaEvento evento, String status) {
        notificacaoResultadoRepository.save(new NotificacaoResultado(
                evento.eventId(), evento.consultaId(), status));
    }
}
