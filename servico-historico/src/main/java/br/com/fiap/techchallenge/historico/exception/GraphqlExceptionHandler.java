package br.com.fiap.techchallenge.historico.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GraphqlExceptionHandler {

    @GraphQlExceptionHandler(AccessDeniedException.class)
    GraphQLError acessoNegado() {
        return erro(ErrorType.FORBIDDEN, "FORBIDDEN", "Acesso negado");
    }

    @GraphQlExceptionHandler(NoSuchElementException.class)
    GraphQLError recursoNaoEncontrado() {
        return erro(ErrorType.NOT_FOUND, "NOT_FOUND", "Recurso não encontrado");
    }

    @GraphQlExceptionHandler(IllegalArgumentException.class)
    GraphQLError entradaInvalida(IllegalArgumentException exception) {
        return erro(ErrorType.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage());
    }

    private GraphQLError erro(ErrorType tipo, String codigo, String mensagem) {
        return GraphqlErrorBuilder.newError()
                .errorType(tipo)
                .message(mensagem)
                .extensions(Map.of("code", codigo))
                .build();
    }
}
