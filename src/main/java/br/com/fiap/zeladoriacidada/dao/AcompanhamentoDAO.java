package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.model.Acompanhamento;
import br.com.fiap.zeladoriacidada.model.StatusOcorrencia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/** Acesso à tabela TB_ZC_ACOMPANHAMENTO. */
@Repository
public class AcompanhamentoDAO {

    private static final String COLUNAS =
            "ID_ACOMPANHAMENTO, ID_OCORRENCIA, DS_COMENTARIO, ST_OCORRENCIA, DT_REGISTRO";

    private final DataSource dataSource;

    public AcompanhamentoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Acompanhamento inserir(Acompanhamento a) {
        String sql = "INSERT INTO TB_ZC_ACOMPANHAMENTO (ID_OCORRENCIA, DS_COMENTARIO, ST_OCORRENCIA, DT_REGISTRO) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_ACOMPANHAMENTO"})) {
            ps.setLong(1, a.getIdOcorrencia());
            ps.setString(2, a.getComentario());
            ps.setString(3, a.getStatus().name());
            JdbcUtil.setTimestamp(ps, 4, a.getDataRegistro());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    a.setId(chaves.getLong(1));
                }
            }
            return a;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao inserir acompanhamento", e);
        }
    }

    public List<Acompanhamento> listarPorOcorrencia(Long idOcorrencia) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_ACOMPANHAMENTO WHERE ID_OCORRENCIA = ? "
                + "ORDER BY DT_REGISTRO, ID_ACOMPANHAMENTO";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                List<Acompanhamento> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao listar acompanhamentos", e);
        }
    }

    public Optional<Acompanhamento> buscarPorId(Long idOcorrencia, Long id) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_ACOMPANHAMENTO "
                + "WHERE ID_OCORRENCIA = ? AND ID_ACOMPANHAMENTO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar acompanhamento", e);
        }
    }

    /** Somente o comentário pode ser corrigido; o status e a data fazem parte do histórico. */
    public boolean atualizarComentario(Long idOcorrencia, Long id, String comentario) {
        String sql = "UPDATE TB_ZC_ACOMPANHAMENTO SET DS_COMENTARIO = ? "
                + "WHERE ID_OCORRENCIA = ? AND ID_ACOMPANHAMENTO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, comentario);
            ps.setLong(2, idOcorrencia);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao atualizar acompanhamento", e);
        }
    }

    public boolean excluir(Long idOcorrencia, Long id) {
        String sql = "DELETE FROM TB_ZC_ACOMPANHAMENTO WHERE ID_OCORRENCIA = ? AND ID_ACOMPANHAMENTO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idOcorrencia);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao excluir acompanhamento", e);
        }
    }

    private Acompanhamento mapear(ResultSet rs) throws SQLException {
        Acompanhamento a = new Acompanhamento();
        a.setId(rs.getLong("ID_ACOMPANHAMENTO"));
        a.setIdOcorrencia(rs.getLong("ID_OCORRENCIA"));
        a.setComentario(rs.getString("DS_COMENTARIO"));
        a.setStatus(StatusOcorrencia.valueOf(rs.getString("ST_OCORRENCIA")));
        a.setDataRegistro(JdbcUtil.paraLocalDateTime(rs.getTimestamp("DT_REGISTRO")));
        return a;
    }
}
