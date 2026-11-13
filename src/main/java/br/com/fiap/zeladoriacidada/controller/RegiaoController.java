package br.com.fiap.zeladoriacidada.controller;

import br.com.fiap.zeladoriacidada.bo.RegiaoBO;
import br.com.fiap.zeladoriacidada.model.Regiao;
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
@RequestMapping("/api/regioes")
public class RegiaoController {

    private final RegiaoBO bo;

    public RegiaoController(RegiaoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public ResponseEntity<List<Regiao>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Regiao> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Regiao> criar(@Valid @RequestBody Regiao regiao) {
        Regiao criada = bo.criar(regiao);
        return ResponseEntity.created(URI.create("/api/regioes/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Regiao> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Regiao regiao) {
        return ResponseEntity.ok(bo.atualizar(id, regiao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
