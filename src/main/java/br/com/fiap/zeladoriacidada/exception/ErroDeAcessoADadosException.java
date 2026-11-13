package br.com.fiap.zeladoriacidada.exception;

/** Falha inesperada ao acessar o banco de dados (HTTP 500). */
public class ErroDeAcessoADadosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErroDeAcessoADadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
