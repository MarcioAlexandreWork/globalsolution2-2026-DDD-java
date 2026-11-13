package br.com.fiap.zeladoriacidada.bo;

import br.com.fiap.zeladoriacidada.dao.RegiaoDAO;
import br.com.fiap.zeladoriacidada.exception.RecursoNaoEncontradoException;
import br.com.fiap.zeladoriacidada.exception.RegraDeNegocioException;
import br.com.fiap.zeladoriacidada.model.Regiao;
import java.util.List;
import org.springframework.stereotype.Service;

/** Regras de negócio de Região. */
@Service
public class RegiaoBO {

    private final RegiaoDAO dao;

    public RegiaoBO(RegiaoDAO dao) {
        this.dao = dao;
    }

    public List<Regiao> listar() {
        return dao.listarTodas();
    }

    public Regiao buscar(Long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Região " + id + " não encontrada."));
    }

    public Regiao criar(Regiao regiao) {
        normalizar(regiao);
        if (dao.existePorNome(regiao.getNome(), null)) {
            throw new RegraDeNegocioException("Já existe uma região com este nome.");
        }
        regiao.setId(null);
        return dao.inserir(regiao);
    }

    public Regiao atualizar(Long id, Regiao dados) {
        buscar(id);
        normalizar(dados);
        if (dao.existePorNome(dados.getNome(), id)) {
            throw new RegraDeNegocioException("Já existe outra região com este nome.");
        }
        dados.setId(id);
        dao.atualizar(dados);
        return buscar(id);
    }

    public void excluir(Long id) {
        if (!dao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Região " + id + " não encontrada.");
        }
    }

    /** Guarda o CEP sempre no formato 00000-000. */
    private void normalizar(Regiao regiao) {
        regiao.setNome(regiao.getNome().trim());
        String cep = regiao.getCep().replace("-", "");
        regiao.setCep(cep.substring(0, 5) + "-" + cep.substring(5));
    }
}
