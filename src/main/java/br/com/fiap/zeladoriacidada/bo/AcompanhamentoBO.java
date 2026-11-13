package br.com.fiap.zeladoriacidada.bo;

import br.com.fiap.zeladoriacidada.dao.AcompanhamentoDAO;
import br.com.fiap.zeladoriacidada.exception.RecursoNaoEncontradoException;
import br.com.fiap.zeladoriacidada.model.Acompanhamento;
import br.com.fiap.zeladoriacidada.model.StatusOcorrencia;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

/** Regras de negócio do histórico (acompanhamentos) de uma ocorrência. */
@Service
public class AcompanhamentoBO {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final AcompanhamentoDAO dao;
    private final OcorrenciaBO ocorrenciaBO;

    public AcompanhamentoBO(AcompanhamentoDAO dao, OcorrenciaBO ocorrenciaBO) {
        this.dao = dao;
        this.ocorrenciaBO = ocorrenciaBO;
    }

    public List<Acompanhamento> listar(Long idOcorrencia) {
        ocorrenciaBO.buscar(idOcorrencia);
        return dao.listarPorOcorrencia(idOcorrencia);
    }

    public Acompanhamento buscar(Long idOcorrencia, Long id) {
        return dao.buscarPorId(idOcorrencia, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Acompanhamento " + id + " não encontrado para a ocorrência " + idOcorrencia + "."));
    }

    /**
     * Registra um novo andamento. Se o status informado for diferente do atual, a ocorrência é atualizada
     * (e a data de resolução é preenchida ao resolver). Sem status, mantém o status atual.
     */
    public Acompanhamento registrar(Long idOcorrencia, Acompanhamento acompanhamento) {
        StatusOcorrencia atual = ocorrenciaBO.buscar(idOcorrencia).getStatus();
        StatusOcorrencia novo = acompanhamento.getStatus() == null ? atual : acompanhamento.getStatus();

        ocorrenciaBO.alterarStatus(idOcorrencia, novo);

        acompanhamento.setId(null);
        acompanhamento.setIdOcorrencia(idOcorrencia);
        acompanhamento.setComentario(acompanhamento.getComentario().trim());
        acompanhamento.setStatus(novo);
        acompanhamento.setDataRegistro(LocalDateTime.now(SAO_PAULO));
        return dao.inserir(acompanhamento);
    }

    public Acompanhamento atualizar(Long idOcorrencia, Long id, Acompanhamento dados) {
        buscar(idOcorrencia, id);
        dao.atualizarComentario(idOcorrencia, id, dados.getComentario().trim());
        return buscar(idOcorrencia, id);
    }

    public void excluir(Long idOcorrencia, Long id) {
        if (!dao.excluir(idOcorrencia, id)) {
            throw new RecursoNaoEncontradoException(
                    "Acompanhamento " + id + " não encontrado para a ocorrência " + idOcorrencia + ".");
        }
    }
}
