package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.model.Regiao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/** Acesso à tabela TB_ZC_REGIAO. */
@Repository
public class RegiaoDAO {

    private static final String COLUNAS = "ID_REGIAO, NM_REGIAO, NR_CEP";

    private final DataSource dataSource;

    public RegiaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Regiao inserir(Regiao regiao) {
        String sql = "INSERT INTO TB_ZC_REGIAO (NM_REGIAO, NR_CEP) VALUES (?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_REGIAO"})) {
            ps.setString(1, regiao.getNome());
            ps.setString(2, regiao.getCep());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    regiao.setId(chaves.getLong(1));
                }
            }
            return regiao;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao inserir região", e);
        }
    }

    public List<Regiao> listarTodas() {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_REGIAO ORDER BY NM_REGIAO";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Regiao> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao listar regiões", e);
        }
    }

    public Optional<Regiao> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_REGIAO WHERE ID_REGIAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar região", e);
        }
    }

    public boolean atualizar(Regiao regiao) {
        String sql = "UPDATE TB_ZC_REGIAO SET NM_REGIAO = ?, NR_CEP = ? WHERE ID_REGIAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regiao.getNome());
            ps.setString(2, regiao.getCep());
            ps.setLong(3, regiao.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao atualizar região", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM TB_ZC_REGIAO WHERE ID_REGIAO = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao excluir região", e);
        }
    }

    /** Verifica se o nome já existe, ignorando o próprio registro (use null ao inserir). */
    public boolean existePorNome(String nome, Long idIgnorado) {
        String sql = "SELECT COUNT(*) FROM TB_ZC_REGIAO WHERE LOWER(NM_REGIAO) = LOWER(?) AND ID_REGIAO <> ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setLong(2, idIgnorado == null ? -1L : idIgnorado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao verificar região", e);
        }
    }

    private Regiao mapear(ResultSet rs) throws SQLException {
        return new Regiao(rs.getLong("ID_REGIAO"), rs.getString("NM_REGIAO"), rs.getString("NR_CEP"));
    }
}
