package br.com.fiap.zeladoriacidada.bo;

import br.com.fiap.zeladoriacidada.dao.CategoriaDAO;
import br.com.fiap.zeladoriacidada.dao.CidadaoDAO;
import br.com.fiap.zeladoriacidada.dao.OcorrenciaDAO;
import br.com.fiap.zeladoriacidada.dao.RegiaoDAO;
import br.com.fiap.zeladoriacidada.exception.RecursoNaoEncontradoException;
import br.com.fiap.zeladoriacidada.exception.RegraDeNegocioException;
import br.com.fiap.zeladoriacidada.model.Ocorrencia;
import br.com.fiap.zeladoriacidada.model.Prioridade;
import br.com.fiap.zeladoriacidada.model.StatusOcorrencia;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

/** Regras de negócio de Ocorrência: criação, ciclo de vida do status e vínculos obrigatórios. */
@Service
public class OcorrenciaBO {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final OcorrenciaDAO dao;
    private final CidadaoDAO cidadaoDAO;
    private final CategoriaDAO categoriaDAO;
    private final RegiaoDAO regiaoDAO;

    public OcorrenciaBO(OcorrenciaDAO dao, CidadaoDAO cidadaoDAO, CategoriaDAO categoriaDAO, RegiaoDAO regiaoDAO) {
        this.dao = dao;
        this.cidadaoDAO = cidadaoDAO;
        this.categoriaDAO = categoriaDAO;
        this.regiaoDAO = regiaoDAO;
    }

    public List<Ocorrencia> listar(StatusOcorrencia status, Long idCategoria, Long idRegiao) {
        return dao.listar(status, idCategoria, idRegiao);
    }

    public Ocorrencia buscar(Long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada."));
    }

    /** Toda ocorrência nasce ABERTA, com a data atual e prioridade MEDIA quando não informada. */
    public Ocorrencia criar(Ocorrencia ocorrencia) {
        validarCidadao(ocorrencia.getIdCidadao());
        validarCategoriaERegiao(ocorrencia.getIdCategoria(), ocorrencia.getIdRegiao());

        ocorrencia.setId(null);
        ocorrencia.setTitulo(ocorrencia.getTitulo().trim());
        ocorrencia.setStatus(StatusOcorrencia.ABERTA);
        if (ocorrencia.getPrioridade() == null) {
            ocorrencia.setPrioridade(Prioridade.MEDIA);
        }
        ocorrencia.setDataAbertura(LocalDateTime.now(SAO_PAULO));
        ocorrencia.setDataResolucao(null);

        Long id = dao.inserir(ocorrencia);
        return buscar(id);
    }

    /** PUT: o cidadão e a data de abertura não mudam; o valor enviado para eles é ignorado. */
    public Ocorrencia atualizar(Long id, Ocorrencia dados) {
        Ocorrencia existente = buscar(id);
        validarCategoriaERegiao(dados.getIdCategoria(), dados.getIdRegiao());

        StatusOcorrencia novoStatus = dados.getStatus() == null ? existente.getStatus() : dados.getStatus();
        validarTransicao(existente.getStatus(), novoStatus);

        existente.setTitulo(dados.getTitulo().trim());
        existente.setDescricao(dados.getDescricao());
        existente.setIdCategoria(dados.getIdCategoria());
        existente.setIdRegiao(dados.getIdRegiao());
        if (dados.getPrioridade() != null) {
            existente.setPrioridade(dados.getPrioridade());
        }
        aplicarStatus(existente, novoStatus);

        dao.atualizar(existente);
        return buscar(id);
    }

    /** Usado também pelos acompanhamentos para refletir o andamento na ocorrência. */
    public Ocorrencia alterarStatus(Long id, StatusOcorrencia novoStatus) {
        Ocorrencia existente = buscar(id);
        validarTransicao(existente.getStatus(), novoStatus);
        if (existente.getStatus() == novoStatus) {
            return existente;
        }
        aplicarStatus(existente, novoStatus);
        dao.atualizar(existente);
        return buscar(id);
    }

    public void excluir(Long id) {
        if (!dao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada.");
        }
    }

    private void validarCidadao(Long idCidadao) {
        if (cidadaoDAO.buscarPorId(idCidadao).isEmpty()) {
            throw new RegraDeNegocioException("O cidadão informado não existe.");
        }
    }

    private void validarCategoriaERegiao(Long idCategoria, Long idRegiao) {
        if (categoriaDAO.buscarPorId(idCategoria).isEmpty()) {
            throw new RegraDeNegocioException("A categoria informada não existe.");
        }
        if (regiaoDAO.buscarPorId(idRegiao).isEmpty()) {
            throw new RegraDeNegocioException("A região informada não existe.");
        }
    }

    /** Uma ocorrência RESOLVIDA é um registro encerrado e não pode ser reaberta. */
    private void validarTransicao(StatusOcorrencia atual, StatusOcorrencia novo) {
        if (atual == StatusOcorrencia.RESOLVIDA && novo != StatusOcorrencia.RESOLVIDA) {
            throw new RegraDeNegocioException("Uma ocorrência resolvida não pode ser reaberta.");
        }
    }

    /** A data de resolução existe somente enquanto o status for RESOLVIDA. */
    private void aplicarStatus(Ocorrencia ocorrencia, StatusOcorrencia novoStatus) {
        ocorrencia.setStatus(novoStatus);
        if (novoStatus == StatusOcorrencia.RESOLVIDA) {
            if (ocorrencia.getDataResolucao() == null) {
                ocorrencia.setDataResolucao(LocalDateTime.now(SAO_PAULO));
            }
        } else {
            ocorrencia.setDataResolucao(null);
        }
    }
}
