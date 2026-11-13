package br.com.fiap.zeladoriacidada.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Tipo de problema urbano (buraco, iluminação, lixo...) e o prazo padrão para resolvê-lo. */
public class Categoria {

    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório.")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
    private String nome;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
    private String descricao;

    @NotNull(message = "O prazo em dias é obrigatório.")
    @Min(value = 1, message = "O prazo deve ser de no mínimo 1 dia.")
    @Max(value = 365, message = "O prazo deve ser de no máximo 365 dias.")
    private Integer prazoDias;

    public Categoria() {
    }

    public Categoria(Long id, String nome, String descricao, Integer prazoDias) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.prazoDias = prazoDias;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getPrazoDias() { return prazoDias; }
    public void setPrazoDias(Integer prazoDias) { this.prazoDias = prazoDias; }
}
