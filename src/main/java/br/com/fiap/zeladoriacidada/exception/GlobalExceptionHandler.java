package br.com.fiap.zeladoriacidada.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Camada de tratamento de exceções: converte qualquer erro em uma resposta JSON padronizada. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException ex,
                                                             HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest req) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", req, detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoIlegivel(HttpMessageNotReadableException ex,
                                                            HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido, mal formatado ou com valor de enum desconhecido.",
                req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                           HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getName() + "' possui um valor inválido.", req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não suportado para este endereço.", req, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMediaType(HttpMediaTypeNotSupportedException ex,
                                                        HttpServletRequest req) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de conteúdo não suportado. Use application/json.", req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Endereço não encontrado.", req, List.of());
    }

    @ExceptionHandler(ErroDeAcessoADadosException.class)
    public ResponseEntity<ErroResposta> tratarAcessoADados(ErroDeAcessoADadosException ex,
                                                           HttpServletRequest req) {
        log.error("Falha de acesso a dados: {}", ex.getMessage(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir a operação no banco de dados. Tente novamente.", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarGenerico(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", req, List.of());
    }

    private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem,
                                                HttpServletRequest req, List<String> detalhes) {
        ErroResposta corpo = new ErroResposta(
                LocalDateTime.now(SAO_PAULO),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                req.getRequestURI(),
                detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
