package br.com.fiap.zeladoriacidada.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Problema do bairro registrado por um cidadão e acompanhado até a resolução. */
public class Ocorrencia {

    private Long id;

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
    private String titulo;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
    private String descricao;

    private StatusOcorrencia status;
    private Prioridade prioridade;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataResolucao;

    @NotNull(message = "O cidadão (idCidadao) é obrigatório.")
    private Long idCidadao;

    @NotNull(message = "A categoria (idCategoria) é obrigatória.")
    private Long idCategoria;

    @NotNull(message = "A região (idRegiao) é obrigatória.")
    private Long idRegiao;

    // Apenas leitura: preenchidos pelo JOIN nas consultas.
    private String nomeCidadao;
    private String nomeCategoria;
    private String nomeRegiao;

    public Ocorrencia() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDateTime dataResolucao) { this.dataResolucao = dataResolucao; }
    public Long getIdCidadao() { return idCidadao; }
    public void setIdCidadao(Long idCidadao) { this.idCidadao = idCidadao; }
    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
    public Long getIdRegiao() { return idRegiao; }
    public void setIdRegiao(Long idRegiao) { this.idRegiao = idRegiao; }
    public String getNomeCidadao() { return nomeCidadao; }
    public void setNomeCidadao(String nomeCidadao) { this.nomeCidadao = nomeCidadao; }
    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }
    public String getNomeRegiao() { return nomeRegiao; }
    public void setNomeRegiao(String nomeRegiao) { this.nomeRegiao = nomeRegiao; }
}
