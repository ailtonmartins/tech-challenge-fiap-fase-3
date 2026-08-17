package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.model.PerfilUsuario;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConsultaAuthorizationService {

    private final UsuarioRepository usuarioRepository;

    public ConsultaAuthorizationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void autorizarGerenciamentoDeConsulta(Authentication authentication) {
        if (possuiPerfil(authentication, PerfilUsuario.MEDICO) || possuiPerfil(authentication, PerfilUsuario.ENFERMEIRO)) {
            return;
        }
        throw new AccessDeniedException("Perfil sem permissão para gerenciar consultas");
    }

    public void autorizarConsultaDeHistorico(Authentication authentication, UUID pacienteIdSolicitado) {
        if (possuiPerfil(authentication, PerfilUsuario.MEDICO) || possuiPerfil(authentication, PerfilUsuario.ENFERMEIRO)) {
            return;
        }

        if (possuiPerfil(authentication, PerfilUsuario.PACIENTE)) {
            Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado"));

            if (pacienteIdSolicitado.equals(usuario.getPacienteId())) {
                return;
            }
        }

        throw new AccessDeniedException("Paciente não possui acesso aos dados solicitados");
    }

    private boolean possuiPerfil(Authentication authentication, PerfilUsuario perfil) {
        String role = "ROLE_" + perfil.name();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
