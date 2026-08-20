package br.com.fiap.techchallenge.historico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/graphql").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(basic -> { })
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate) {
        return username -> {
            var usuario = jdbcTemplate.query(
                "SELECT username, password_hash, role, enabled FROM agendamento.usuario WHERE username = ?",
                resultSet -> resultSet.next()
                        ? User.withUsername(resultSet.getString("username"))
                        .password(resultSet.getString("password_hash"))
                        .roles(resultSet.getString("role"))
                        .disabled(!resultSet.getBoolean("enabled"))
                        .build()
                        : null,
                username
            );
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuário não encontrado");
            }
            return usuario;
        };
    }
}
