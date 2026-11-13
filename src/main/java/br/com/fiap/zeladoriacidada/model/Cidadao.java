package br.com.fiap.zeladoriacidada.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Pessoa que usa o sistema: morador que reporta ou agente que atende. */
public class Cidadao {

    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
    private String email;

    @Pattern(regexp = "^$|^\\d{10,11}$", message = "O telefone deve ter 10 ou 11 dígitos, apenas números.")
    private String telefone;

    @NotNull(message = "O tipo é obrigatório (MORADOR ou AGENTE).")
    private TipoCidadao tipo;

    public Cidadao() {
    }

    public Cidadao(Long id, String nome, String email, String telefone, TipoCidadao tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public TipoCidadao getTipo() { return tipo; }
    public void setTipo(TipoCidadao tipo) { this.tipo = tipo; }
}
