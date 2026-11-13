package br.com.fiap.zeladoriacidada.bo;

import br.com.fiap.zeladoriacidada.dao.CategoriaDAO;
import br.com.fiap.zeladoriacidada.exception.RecursoNaoEncontradoException;
import br.com.fiap.zeladoriacidada.exception.RegraDeNegocioException;
import br.com.fiap.zeladoriacidada.model.Categoria;
import java.util.List;
import org.springframework.stereotype.Service;

/** Regras de negócio de Categoria. */
@Service
public class CategoriaBO {

    private final CategoriaDAO dao;

    public CategoriaBO(CategoriaDAO dao) {
        this.dao = dao;
    }

    public List<Categoria> listar() {
        return dao.listarTodas();
    }

    public Categoria buscar(Long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada."));
    }

    public Categoria criar(Categoria categoria) {
        categoria.setNome(categoria.getNome().trim());
        if (dao.existePorNome(categoria.getNome(), null)) {
            throw new RegraDeNegocioException("Já existe uma categoria com este nome.");
        }
        categoria.setId(null);
        return dao.inserir(categoria);
    }

    public Categoria atualizar(Long id, Categoria dados) {
        buscar(id);
        dados.setNome(dados.getNome().trim());
        if (dao.existePorNome(dados.getNome(), id)) {
            throw new RegraDeNegocioException("Já existe outra categoria com este nome.");
        }
        dados.setId(id);
        dao.atualizar(dados);
        return buscar(id);
    }

    public void excluir(Long id) {
        if (!dao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Categoria " + id + " não encontrada.");
        }
    }
}
