package br.com.fiap.techchallenge.agendamento.model;

public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    REALIZADA,
    CANCELADA;

    public boolean podeTransicionarPara(StatusConsulta destino) {
        return switch (this) {
            case AGENDADA -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == REALIZADA || destino == CANCELADA;
            case REALIZADA, CANCELADA -> false;
        };
    }
}
