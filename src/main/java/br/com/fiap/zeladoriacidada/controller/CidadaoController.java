package br.com.fiap.zeladoriacidada.controller;

import br.com.fiap.zeladoriacidada.bo.CidadaoBO;
import br.com.fiap.zeladoriacidada.model.Cidadao;
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
@RequestMapping("/api/cidadaos")
public class CidadaoController {

    private final CidadaoBO bo;

    public CidadaoController(CidadaoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public ResponseEntity<List<Cidadao>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cidadao> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Cidadao> criar(@Valid @RequestBody Cidadao cidadao) {
        Cidadao criado = bo.criar(cidadao);
        return ResponseEntity.created(URI.create("/api/cidadaos/" + criado.getId())).body(criado);
    }

    /** Identifica quem está registrando: reaproveita o cadastro pelo e-mail ou cria um novo MORADOR. */
    @PostMapping("/identificar")
    public ResponseEntity<Cidadao> identificar(@Valid @RequestBody Cidadao cidadao) {
        return ResponseEntity.ok(bo.identificar(cidadao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cidadao> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Cidadao cidadao) {
        return ResponseEntity.ok(bo.atualizar(id, cidadao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
