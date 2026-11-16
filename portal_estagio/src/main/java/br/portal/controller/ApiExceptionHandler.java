package br.portal.controller;

import br.portal.excecao.AcessoNegadoException;
import br.portal.excecao.EntidadeNaoEncontradaException;
import br.portal.excecao.PersistenciaException;
import br.portal.excecao.RegraNegocioException;
import br.portal.excecao.ValidacaoException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

/**
 * Tratamento global de erros: transforma exceções em respostas JSON com o código HTTP certo.
 * Mensagens de regra de negócio são escritas para o usuário e podem ser mostradas.
 * Erros inesperados e de banco NUNCA mostram detalhes: só vão para o log do servidor.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String erro) {
        return ResponseEntity.status(status).body(Map.of("erro", erro));
    }

    // ---- 400: requisição inválida ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> corpoInvalido(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos.", "detalhes", detalhes));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<Map<String, Object>> parametroInvalido(HandlerMethodValidationException e) {
        List<String> detalhes = e.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos.", "detalhes", detalhes));
    }

    @ExceptionHandler({ValidacaoException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception e) {
        String msg = e instanceof ValidacaoException ? e.getMessage() : "Requisição inválida.";
        return resposta(HttpStatus.BAD_REQUEST, msg);
    }

    // ---- 401 / 403 ----
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> naoAutenticado(AuthenticationException e) {
        // mensagem única: não revela se o e-mail existe
        return resposta(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    ResponseEntity<Map<String, Object>> proibido(Exception e) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado.");
    }

    // ---- 404 ----
    @ExceptionHandler({EntidadeNaoEncontradaException.class, NoResourceFoundException.class})
    ResponseEntity<Map<String, Object>> naoEncontrado(Exception e) {
        String msg = e instanceof EntidadeNaoEncontradaException ? e.getMessage() : "Recurso não encontrado.";
        return resposta(HttpStatus.NOT_FOUND, msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> metodoNaoPermitido(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido.");
    }

    // ---- 409: conflito com regra de negócio ou com o estado atual ----
    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<Map<String, Object>> regraDeNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade no banco", e);
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.");
    }

    // ---- demais códigos já escolhidos no código (ex.: 401 do ControllerSupport) ----
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> comStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        return resposta(status != null ? status : HttpStatus.BAD_REQUEST,
                status == HttpStatus.UNAUTHORIZED ? "Autenticação necessária." : "Requisição inválida.");
    }

    // ---- 500: nunca expõe detalhes ----
    @ExceptionHandler(PersistenciaException.class)
    ResponseEntity<Map<String, Object>> persistencia(PersistenciaException e) {
        log.error("Falha de persistência", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }
}
