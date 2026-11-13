package br.com.fiap.zeladoriacidada.controller;

import br.com.fiap.zeladoriacidada.bo.OcorrenciaBO;
import br.com.fiap.zeladoriacidada.model.Ocorrencia;
import br.com.fiap.zeladoriacidada.model.StatusOcorrencia;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaBO bo;

    public OcorrenciaController(OcorrenciaBO bo) {
        this.bo = bo;
    }

    /** Filtros opcionais: ?status=ABERTA&idCategoria=1&idRegiao=2 */
    @GetMapping
    public ResponseEntity<List<Ocorrencia>> listar(
            @RequestParam(name = "status", required = false) StatusOcorrencia status,
            @RequestParam(name = "idCategoria", required = false) Long idCategoria,
            @RequestParam(name = "idRegiao", required = false) Long idRegiao) {
        return ResponseEntity.ok(bo.listar(status, idCategoria, idRegiao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ocorrencia> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Ocorrencia> criar(@Valid @RequestBody Ocorrencia ocorrencia) {
        Ocorrencia criada = bo.criar(ocorrencia);
        return ResponseEntity.created(URI.create("/api/ocorrencias/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ocorrencia> atualizar(@PathVariable("id") Long id,
                                                @Valid @RequestBody Ocorrencia ocorrencia) {
        return ResponseEntity.ok(bo.atualizar(id, ocorrencia));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
