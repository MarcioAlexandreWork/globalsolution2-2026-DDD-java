package br.com.fiap.zeladoriacidada.exception;

/** Requisição válida, mas que viola uma regra de negócio (HTTP 422). */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
