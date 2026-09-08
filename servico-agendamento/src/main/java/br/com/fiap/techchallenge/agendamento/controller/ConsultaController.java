package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.dto.AtualizarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.AtualizarStatusConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.dto.CriarConsultaRequest;
import br.com.fiap.techchallenge.agendamento.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> criar(
            @Valid @RequestBody CriarConsultaRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultaService.criar(request, authentication));
    }

    @PutMapping("/{id}")
    public ConsultaResponse atualizar(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody AtualizarConsultaRequest request,
            Authentication authentication) {
        return consultaService.atualizar(id, request, authentication);
    }

    @PatchMapping("/{id}/status")
    public ConsultaResponse atualizarStatus(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody AtualizarStatusConsultaRequest request,
            Authentication authentication) {
        return consultaService.atualizarStatus(id, request, authentication);
    }
}
