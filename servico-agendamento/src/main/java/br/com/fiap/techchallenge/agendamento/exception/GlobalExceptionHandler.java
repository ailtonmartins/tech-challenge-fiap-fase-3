package br.com.fiap.techchallenge.agendamento.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErroResponse> acessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroResponse("FORBIDDEN", "Acesso negado"));
    }

    @ExceptionHandler(PacienteEmailDuplicadoException.class)
    ResponseEntity<ErroResponse> emailDuplicado() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Já existe um paciente cadastrado com este e-mail"));
    }

    @ExceptionHandler(PacienteNaoEncontradoException.class)
    ResponseEntity<ErroResponse> pacienteNaoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("NOT_FOUND", "Paciente não encontrado"));
    }

    @ExceptionHandler(ConflitoDeAgendamentoException.class)
    ResponseEntity<ErroResponse> conflitoDeAgendamento() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Já existe uma consulta agendada para o paciente neste horário"));
    }

    @ExceptionHandler(ConsultaNaoEncontradaException.class)
    ResponseEntity<ErroResponse> consultaNaoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse("NOT_FOUND", "Consulta não encontrada"));
    }

    @ExceptionHandler(ConsultaNaoPodeSerAlteradaException.class)
    ResponseEntity<ErroResponse> consultaNaoPodeSerAlterada() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Consultas canceladas ou realizadas não podem ser alteradas"));
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    ResponseEntity<ErroResponse> transicaoDeStatusInvalida() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Transição de status da consulta não permitida"));
    }

    @ExceptionHandler(ConflitoDeAtualizacaoException.class)
    ResponseEntity<ErroResponse> conflitoDeAtualizacao() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "A consulta foi alterada por outro usuário"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> violacaoDeIntegridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Não foi possível concluir a operação devido a dados duplicados"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> entradaInvalida(MethodArgumentNotValidException exception) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(erro ->
                detalhes.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ErroResponse("VALIDATION_ERROR", "Dados de entrada inválidos", detalhes));
    }

    record ErroResponse(String code, String message, Map<String, String> details) {
        ErroResponse(String code, String message) {
            this(code, message, Map.of());
        }
    }
}
