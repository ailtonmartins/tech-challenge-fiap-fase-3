package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.notificacao.dto.ConsultaEvento;
import br.com.fiap.techchallenge.notificacao.exception.EventoConsultaInvalidoException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ConsultaEventoValidator {

    private static final int VERSAO_SUPORTADA = 1;
    private static final String EVENTO_CRIADO = "CONSULTA_CRIADA";
    private static final String EVENTO_ATUALIZADO = "CONSULTA_ATUALIZADA";
    private static final Set<String> TIPOS_SUPORTADOS = Set.of(EVENTO_CRIADO, EVENTO_ATUALIZADO);

    public void validar(ConsultaEvento evento, String topico) {
        if (evento == null) {
            throw invalido("Evento de consulta ausente");
        }
        if (evento.eventVersion() != VERSAO_SUPORTADA) {
            throw invalido("Versão de evento não suportada: " + evento.eventVersion());
        }
        if (emBranco(evento.eventType()) || !TIPOS_SUPORTADOS.contains(evento.eventType())) {
            throw invalido("Tipo de evento não suportado: " + evento.eventType());
        }
        if (evento.eventId() == null || evento.occurredAt() == null || evento.consultaId() == null
                || evento.pacienteId() == null || evento.dataHora() == null
                || emBranco(evento.medico()) || emBranco(evento.especialidade()) || emBranco(evento.status())) {
            throw invalido("Evento de consulta sem campos obrigatórios");
        }
        if ("consulta.criada.v1".equals(topico) && !EVENTO_CRIADO.equals(evento.eventType())) {
            throw invalido("Tipo de evento incompatível com o tópico de criação");
        }
        if ("consulta.atualizada.v1".equals(topico) && !EVENTO_ATUALIZADO.equals(evento.eventType())) {
            throw invalido("Tipo de evento incompatível com o tópico de atualização");
        }
    }

    private boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }

    private EventoConsultaInvalidoException invalido(String mensagem) {
        return new EventoConsultaInvalidoException(mensagem);
    }
}
