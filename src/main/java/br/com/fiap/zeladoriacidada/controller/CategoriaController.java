package br.com.fiap.zeladoriacidada.controller;

import br.com.fiap.zeladoriacidada.bo.CategoriaBO;
import br.com.fiap.zeladoriacidada.model.Categoria;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaBO bo;

    public CategoriaController(CategoriaBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(bo.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bo.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria) {
        Categoria criada = bo.criar(categoria);
        return ResponseEntity.created(URI.create("/api/categorias/" + criada.getId())).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Categoria categoria) {
        return ResponseEntity.ok(bo.atualizar(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
