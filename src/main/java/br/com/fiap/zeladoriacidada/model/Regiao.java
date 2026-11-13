package br.com.fiap.zeladoriacidada.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Trecho de Santana (rua ou avenida) onde as ocorrências são registradas. */
public class Regiao {

    private Long id;

    @NotBlank(message = "O nome da região é obrigatório.")
    @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
    private String nome;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "O CEP deve estar no formato 00000-000.")
    private String cep;

    public Regiao() {
    }

    public Regiao(Long id, String nome, String cep) {
        this.id = id;
        this.nome = nome;
        this.cep = cep;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
}
