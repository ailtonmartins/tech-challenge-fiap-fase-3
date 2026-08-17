package br.com.fiap.techchallenge.agendamento.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @GetMapping("/me")
    public UsuarioAutenticadoResponse usuarioAutenticado(Authentication authentication) {
        String perfil = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("SEM_PERFIL");

        return new UsuarioAutenticadoResponse(authentication.getName(), perfil);
    }

    public record UsuarioAutenticadoResponse(String username, String perfil) {
    }
}
