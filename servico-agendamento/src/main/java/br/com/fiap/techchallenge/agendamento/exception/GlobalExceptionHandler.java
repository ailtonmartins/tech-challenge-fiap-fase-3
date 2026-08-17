package br.com.fiap.techchallenge.agendamento.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> violacaoDeIntegridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse("CONFLICT", "Não foi possível concluir a operação devido a dados duplicados"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> entradaInvalida() {
        return ResponseEntity.badRequest()
                .body(new ErroResponse("VALIDATION_ERROR", "Dados de entrada inválidos"));
    }

    record ErroResponse(String code, String message) {
    }
}
