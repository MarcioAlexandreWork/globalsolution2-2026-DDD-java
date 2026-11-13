package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/** Acesso à tabela TB_ZC_CATEGORIA. */
@Repository
public class CategoriaDAO {

    private static final String COLUNAS = "ID_CATEGORIA, NM_CATEGORIA, DS_CATEGORIA, NR_PRAZO_DIAS";

    private final DataSource dataSource;

    public CategoriaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Categoria inserir(Categoria categoria) {
        String sql = "INSERT INTO TB_ZC_CATEGORIA (NM_CATEGORIA, DS_CATEGORIA, NR_PRAZO_DIAS) VALUES (?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_CATEGORIA"})) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setInt(3, categoria.getPrazoDias());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    categoria.setId(chaves.getLong(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao inserir categoria", e);
        }
    }

    public List<Categoria> listarTodas() {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_CATEGORIA ORDER BY NM_CATEGORIA";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao listar categorias", e);
        }
    }

    public Optional<Categoria> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM TB_ZC_CATEGORIA WHERE ID_CATEGORIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar categoria", e);
        }
    }

    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE TB_ZC_CATEGORIA SET NM_CATEGORIA = ?, DS_CATEGORIA = ?, NR_PRAZO_DIAS = ? "
                + "WHERE ID_CATEGORIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setInt(3, categoria.getPrazoDias());
            ps.setLong(4, categoria.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao atualizar categoria", e);
        }
    }

    public boolean excluir(Long id) {
        String sql = "DELETE FROM TB_ZC_CATEGORIA WHERE ID_CATEGORIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao excluir categoria", e);
        }
    }

    /** Verifica se o nome já existe, ignorando o próprio registro (use null ao inserir). */
    public boolean existePorNome(String nome, Long idIgnorado) {
        String sql = "SELECT COUNT(*) FROM TB_ZC_CATEGORIA WHERE LOWER(NM_CATEGORIA) = LOWER(?) AND ID_CATEGORIA <> ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setLong(2, idIgnorado == null ? -1L : idIgnorado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao verificar categoria", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getLong("ID_CATEGORIA"),
                rs.getString("NM_CATEGORIA"),
                rs.getString("DS_CATEGORIA"),
                rs.getInt("NR_PRAZO_DIAS"));
    }
}
