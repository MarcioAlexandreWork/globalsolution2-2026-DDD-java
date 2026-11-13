package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.model.Cidadao;
import br.com.fiap.zeladoriacidada.model.TipoCidadao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/** Acesso à tabela TB_ZC_CIDADAO. */
@Repository
public class CidadaoDAO {

    private static final String COLUNAS = "ID_CIDADAO, NM_CIDADAO, DS_EMAIL, NR_TELEFONE, TP_CIDADAO";

    private final DataSource dataSource;

    public CidadaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Cidadao inserir(Cidadao cidadao) {
        String sql = "INSERT INTO TB_ZC_CIDADAO (NM_CIDADAO, DS_EMAIL, NR_TELEFONE, TP_CIDADAO) VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_CIDADAO"})) {
            ps.setString(1, cidadao.getNome());
            ps.setString(2, cidadao.getEmail());
            ps.setString(3, cidadao.getTelefone());
            ps.setString(4, cidadao.getTipo().name());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    cidadao.setId(chaves.getLong(1));
                }
            }
            return cidadao;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao inserir cidadão", e);
        }
    }

    public List<Cidadao> listarTodos() {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_CIDADAO ORDER BY NM_CIDADAO";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Cidadao> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao listar cidadãos", e);
        }
    }

    public Optional<Cidadao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_CIDADAO WHERE ID_CIDADAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar cidadão", e);
        }
    }

    public Optional<Cidadao> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_CIDADAO WHERE LOWER(DS_EMAIL) = LOWER(?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar cidadão por e-mail", e);
        }
    }

    public boolean atualizar(Cidadao cidadao) {
        String sql = "UPDATE TB_ZC_CIDADAO SET NM_CIDADAO = ?, DS_EMAIL = ?, NR_TELEFONE = ?, TP_CIDADAO = ? "
                + "WHERE ID_CIDADAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cidadao.getNome());
            ps.setString(2, cidadao.getEmail());
            ps.setString(3, cidadao.getTelefone());
            ps.setString(4, cidadao.getTipo().name());
            ps.setLong(5, cidadao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao atualizar cidadão", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM TB_ZC_CIDADAO WHERE ID_CIDADAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao excluir cidadão", e);
        }
    }

    /** Verifica se o e-mail já existe, ignorando o próprio registro (use null ao inserir). */
    public boolean existePorEmail(String email, Long idIgnorado) {
        String sql = "SELECT COUNT(*) FROM TB_ZC_CIDADAO WHERE LOWER(DS_EMAIL) = LOWER(?) AND ID_CIDADAO <> ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setLong(2, idIgnorado == null ? -1L : idIgnorado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao verificar e-mail", e);
        }
    }

    private Cidadao mapear(ResultSet rs) throws SQLException {
        return new Cidadao(
                rs.getLong("ID_CIDADAO"),
                rs.getString("NM_CIDADAO"),
                rs.getString("DS_EMAIL"),
                rs.getString("NR_TELEFONE"),
                TipoCidadao.valueOf(rs.getString("TP_CIDADAO")));
    }
}
