package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.model.PerfilUsuario;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void deveCarregarUsuarioComPerfilEHashBcrypt() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hash = passwordEncoder.encode("Senha@123");
        Usuario usuario = new Usuario("medico.ana", hash, PerfilUsuario.MEDICO, true, null);
        when(usuarioRepository.findByUsername("medico.ana")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("medico.ana");

        assertEquals("medico.ana", userDetails.getUsername());
        assertTrue(passwordEncoder.matches("Senha@123", userDetails.getPassword()));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MEDICO")));
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("inexistente"));
    }
}
