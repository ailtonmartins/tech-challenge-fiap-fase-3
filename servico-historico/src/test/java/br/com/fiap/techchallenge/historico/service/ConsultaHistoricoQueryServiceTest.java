package br.com.fiap.techchallenge.historico.service;

import br.com.fiap.techchallenge.historico.repository.ConsultaHistoricoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;

@ExtendWith(MockitoExtension.class)
class ConsultaHistoricoQueryServiceTest {

    @Mock
    private ConsultaHistoricoRepository repository;

    @Test
    void deveRejeitarTamanhoAcimaDoLimite() {
        var service = new ConsultaHistoricoQueryService(repository, 50);

        assertThatThrownBy(() -> service.historicoDoPaciente(UUID.randomUUID(), 0, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Paginação inválida");
    }

    @Test
    void deveBuscarSomenteConsultasComDataFutura() {
        var service = new ConsultaHistoricoQueryService(repository, 50);
        UUID pacienteId = UUID.randomUUID();
        when(repository.findByPacienteIdAndDataHoraAfter(org.mockito.ArgumentMatchers.eq(pacienteId),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(empty());

        service.consultasFuturas(pacienteId, 1, 10);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByPacienteIdAndDataHoraAfter(
                org.mockito.ArgumentMatchers.eq(pacienteId),
                org.mockito.ArgumentMatchers.any(OffsetDateTime.class), pageable.capture());
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
    }
}
