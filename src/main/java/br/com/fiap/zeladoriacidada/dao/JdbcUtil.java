package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.exception.ErroDeAcessoADadosException;
import br.com.fiap.zeladoriacidada.exception.RegraDeNegocioException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilitários JDBC compartilhados pelos DAOs (conversões e tradução de erros do Oracle). */
final class JdbcUtil {

    private static final Logger log = LoggerFactory.getLogger(JdbcUtil.class);

    private static final int ORA_UNIQUE_VIOLADA = 1;
    private static final int ORA_PAI_NAO_ENCONTRADO = 2291;
    private static final int ORA_FILHO_ENCONTRADO = 2292;

    private JdbcUtil() {
    }

    static void setTimestamp(PreparedStatement ps, int indice, LocalDateTime valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, Timestamp.valueOf(valor));
        }
    }

    static LocalDateTime paraLocalDateTime(Timestamp valor) {
        return valor == null ? null : valor.toLocalDateTime();
    }

    /** Traduz erros conhecidos do Oracle em regras de negócio; os demais viram erro de acesso a dados. */
    static RuntimeException traduzir(String mensagem, SQLException e) {
        switch (e.getErrorCode()) {
            case ORA_UNIQUE_VIOLADA:
                return new RegraDeNegocioException("Já existe um registro com os mesmos dados únicos.");
            case ORA_PAI_NAO_ENCONTRADO:
                return new RegraDeNegocioException("Uma das referências informadas não existe.");
            case ORA_FILHO_ENCONTRADO:
                return new RegraDeNegocioException(
                        "O registro possui vínculos com outros dados e não pode ser excluído.");
            default:
                log.error("{} (ORA-{})", mensagem, e.getErrorCode());
                return new ErroDeAcessoADadosException(mensagem, e);
        }
    }
}
