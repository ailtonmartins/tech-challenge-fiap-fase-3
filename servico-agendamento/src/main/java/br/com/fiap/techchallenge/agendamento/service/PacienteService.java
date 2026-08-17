package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.CadastrarPacienteRequest;
import br.com.fiap.techchallenge.agendamento.dto.PacienteResponse;
import br.com.fiap.techchallenge.agendamento.exception.PacienteEmailDuplicadoException;
import br.com.fiap.techchallenge.agendamento.model.Paciente;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ConsultaAuthorizationService authorizationService;

    public PacienteService(PacienteRepository pacienteRepository, ConsultaAuthorizationService authorizationService) {
        this.pacienteRepository = pacienteRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public PacienteResponse cadastrar(CadastrarPacienteRequest request, Authentication authentication) {
        authorizationService.autorizarGerenciamentoDeConsulta(authentication);

        String emailNormalizado = request.email().trim().toLowerCase(Locale.ROOT);
        if (pacienteRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new PacienteEmailDuplicadoException();
        }

        Paciente paciente = new Paciente(
                request.nome().trim(),
                emailNormalizado,
                request.telefone().trim(),
                request.dataNascimento());

        return PacienteResponse.from(pacienteRepository.save(paciente));
    }
}
