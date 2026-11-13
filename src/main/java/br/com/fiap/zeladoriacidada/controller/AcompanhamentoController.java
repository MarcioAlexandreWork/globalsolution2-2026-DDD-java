package br.com.fiap.zeladoriacidada.controller;

import br.com.fiap.zeladoriacidada.bo.AcompanhamentoBO;
import br.com.fiap.zeladoriacidada.model.Acompanhamento;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocorrencias/{idOcorrencia}/acompanhamentos")
public class AcompanhamentoController {

    private final AcompanhamentoBO bo;

    public AcompanhamentoController(AcompanhamentoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public ResponseEntity<List<Acompanhamento>> listar(@PathVariable("idOcorrencia") Long idOcorrencia) {
        return ResponseEntity.ok(bo.listar(idOcorrencia));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Acompanhamento> buscar(@PathVariable("idOcorrencia") Long idOcorrencia,
                                                 @PathVariable("id") Long id) {
        return ResponseEntity.ok(bo.buscar(idOcorrencia, id));
    }

    @PostMapping
    public ResponseEntity<Acompanhamento> registrar(@PathVariable("idOcorrencia") Long idOcorrencia,
                                                    @Valid @RequestBody Acompanhamento acompanhamento) {
        Acompanhamento criado = bo.registrar(idOcorrencia, acompanhamento);
        URI local = URI.create("/api/ocorrencias/" + idOcorrencia + "/acompanhamentos/" + criado.getId());
        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Acompanhamento> atualizar(@PathVariable("idOcorrencia") Long idOcorrencia,
                                                    @PathVariable("id") Long id,
                                                    @Valid @RequestBody Acompanhamento acompanhamento) {
        return ResponseEntity.ok(bo.atualizar(idOcorrencia, id, acompanhamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("idOcorrencia") Long idOcorrencia,
                                        @PathVariable("id") Long id) {
        bo.excluir(idOcorrencia, id);
        return ResponseEntity.noContent().build();
    }
}
