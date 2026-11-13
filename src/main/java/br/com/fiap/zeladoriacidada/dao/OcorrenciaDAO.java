package br.com.fiap.zeladoriacidada.dao;

import br.com.fiap.zeladoriacidada.model.Ocorrencia;
import br.com.fiap.zeladoriacidada.model.Prioridade;
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

/** Acesso à tabela TB_ZC_OCORRENCIA (consultas com JOIN para trazer os nomes relacionados). */
@Repository
public class OcorrenciaDAO {

    private static final String SELECT_BASE =
            "SELECT o.ID_OCORRENCIA, o.DS_TITULO, o.DS_OCORRENCIA, o.ST_OCORRENCIA, o.DS_PRIORIDADE, "
            + "o.DT_ABERTURA, o.DT_RESOLUCAO, o.ID_CIDADAO, o.ID_CATEGORIA, o.ID_REGIAO, "
            + "c.NM_CIDADAO, ca.NM_CATEGORIA, r.NM_REGIAO "
            + "FROM TB_ZC_OCORRENCIA o "
            + "JOIN TB_ZC_CIDADAO c ON c.ID_CIDADAO = o.ID_CIDADAO "
            + "JOIN TB_ZC_CATEGORIA ca ON ca.ID_CATEGORIA = o.ID_CATEGORIA "
            + "JOIN TB_ZC_REGIAO r ON r.ID_REGIAO = o.ID_REGIAO ";

    private final DataSource dataSource;

    public OcorrenciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long inserir(Ocorrencia o) {
        String sql = "INSERT INTO TB_ZC_OCORRENCIA (DS_TITULO, DS_OCORRENCIA, ST_OCORRENCIA, DS_PRIORIDADE, "
                + "DT_ABERTURA, DT_RESOLUCAO, ID_CIDADAO, ID_CATEGORIA, ID_REGIAO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"ID_OCORRENCIA"})) {
            ps.setString(1, o.getTitulo());
            ps.setString(2, o.getDescricao());
            ps.setString(3, o.getStatus().name());
            ps.setString(4, o.getPrioridade().name());
            JdbcUtil.setTimestamp(ps, 5, o.getDataAbertura());
            JdbcUtil.setTimestamp(ps, 6, o.getDataResolucao());
            ps.setLong(7, o.getIdCidadao());
            ps.setLong(8, o.getIdCategoria());
            ps.setLong(9, o.getIdRegiao());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getLong(1) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao inserir ocorrência", e);
        }
    }

    /** Lista com filtros opcionais (qualquer filtro nulo é ignorado), da mais recente para a mais antiga. */
    public List<Ocorrencia> listar(StatusOcorrencia status, Long idCategoria, Long idRegiao) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (status != null) {
            sql.append("AND o.ST_OCORRENCIA = ? ");
            parametros.add(status.name());
        }
        if (idCategoria != null) {
            sql.append("AND o.ID_CATEGORIA = ? ");
            parametros.add(idCategoria);
        }
        if (idRegiao != null) {
            sql.append("AND o.ID_REGIAO = ? ");
            parametros.add(idRegiao);
        }
        sql.append("ORDER BY o.DT_ABERTURA DESC, o.ID_OCORRENCIA DESC");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);
                if (valor instanceof Long l) {
                    ps.setLong(i + 1, l);
                } else {
                    ps.setString(i + 1, (String) valor);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Ocorrencia> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao listar ocorrências", e);
        }
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE o.ID_OCORRENCIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao buscar ocorrência", e);
        }
    }

    /** O cidadão que abriu e a data de abertura são imutáveis, por isso não entram no UPDATE. */
    public boolean atualizar(Ocorrencia o) {
        String sql = "UPDATE TB_ZC_OCORRENCIA SET DS_TITULO = ?, DS_OCORRENCIA = ?, ST_OCORRENCIA = ?, "
                + "DS_PRIORIDADE = ?, DT_RESOLUCAO = ?, ID_CATEGORIA = ?, ID_REGIAO = ? WHERE ID_OCORRENCIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, o.getTitulo());
            ps.setString(2, o.getDescricao());
            ps.setString(3, o.getStatus().name());
            ps.setString(4, o.getPrioridade().name());
            JdbcUtil.setTimestamp(ps, 5, o.getDataResolucao());
            ps.setLong(6, o.getIdCategoria());
            ps.setLong(7, o.getIdRegiao());
            ps.setLong(8, o.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao atualizar ocorrência", e);
        }
    }

    /** Os acompanhamentos são removidos em cascata pelo banco (ON DELETE CASCADE). */
    public boolean excluir(Long id) {
        String sql = "DELETE FROM TB_ZC_OCORRENCIA WHERE ID_OCORRENCIA = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.traduzir("Erro ao excluir ocorrência", e);
        }
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("ID_OCORRENCIA"));
        o.setTitulo(rs.getString("DS_TITULO"));
        o.setDescricao(rs.getString("DS_OCORRENCIA"));
        o.setStatus(StatusOcorrencia.valueOf(rs.getString("ST_OCORRENCIA")));
        o.setPrioridade(Prioridade.valueOf(rs.getString("DS_PRIORIDADE")));
        o.setDataAbertura(JdbcUtil.paraLocalDateTime(rs.getTimestamp("DT_ABERTURA")));
        o.setDataResolucao(JdbcUtil.paraLocalDateTime(rs.getTimestamp("DT_RESOLUCAO")));
        o.setIdCidadao(rs.getLong("ID_CIDADAO"));
        o.setIdCategoria(rs.getLong("ID_CATEGORIA"));
        o.setIdRegiao(rs.getLong("ID_REGIAO"));
        o.setNomeCidadao(rs.getString("NM_CIDADAO"));
        o.setNomeCategoria(rs.getString("NM_CATEGORIA"));
        o.setNomeRegiao(rs.getString("NM_REGIAO"));
        return o;
    }
}
