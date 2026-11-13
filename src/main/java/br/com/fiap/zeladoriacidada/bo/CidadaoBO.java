package br.com.fiap.zeladoriacidada.bo;

import br.com.fiap.zeladoriacidada.dao.CidadaoDAO;
import br.com.fiap.zeladoriacidada.exception.RecursoNaoEncontradoException;
import br.com.fiap.zeladoriacidada.exception.RegraDeNegocioException;
import br.com.fiap.zeladoriacidada.model.Cidadao;
import br.com.fiap.zeladoriacidada.model.TipoCidadao;
import java.util.List;
import org.springframework.stereotype.Service;

/** Regras de negócio de Cidadão. */
@Service
public class CidadaoBO {

    private final CidadaoDAO dao;

    public CidadaoBO(CidadaoDAO dao) {
        this.dao = dao;
    }

    public List<Cidadao> listar() {
        return dao.listarTodos();
    }

    public Cidadao buscar(Long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão " + id + " não encontrado."));
    }

    public Cidadao criar(Cidadao cidadao) {
        normalizar(cidadao);
        if (dao.existePorEmail(cidadao.getEmail(), null)) {
            throw new RegraDeNegocioException("Já existe um cidadão cadastrado com este e-mail.");
        }
        cidadao.setId(null);
        return dao.inserir(cidadao);
    }

    /**
     * Fluxo público de "quem está registrando": se o e-mail já existe, devolve o cadastro existente;
     * caso contrário, cria um novo cidadão sempre como MORADOR (ninguém se cadastra como AGENTE sozinho).
     */
    public Cidadao identificar(Cidadao dados) {
        normalizar(dados);
        return dao.buscarPorEmail(dados.getEmail()).orElseGet(() -> {
            dados.setId(null);
            dados.setTipo(TipoCidadao.MORADOR);
            return dao.inserir(dados);
        });
    }

    public Cidadao atualizar(Long id, Cidadao dados) {
        buscar(id);
        normalizar(dados);
        if (dao.existePorEmail(dados.getEmail(), id)) {
            throw new RegraDeNegocioException("Já existe outro cidadão cadastrado com este e-mail.");
        }
        dados.setId(id);
        dao.atualizar(dados);
        return buscar(id);
    }

    public void excluir(Long id) {
        if (!dao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Cidadão " + id + " não encontrado.");
        }
    }

    private void normalizar(Cidadao cidadao) {
        cidadao.setNome(cidadao.getNome().trim());
        cidadao.setEmail(cidadao.getEmail().trim().toLowerCase());
    }
}
