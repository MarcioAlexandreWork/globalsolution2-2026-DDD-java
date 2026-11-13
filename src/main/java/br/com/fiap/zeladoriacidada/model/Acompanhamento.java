package br.com.fiap.zeladoriacidada.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Registro do histórico de uma ocorrência, o que o cidadão acompanha até a resolução. */
public class Acompanhamento {

    private Long id;
    private Long idOcorrencia;

    @NotBlank(message = "O comentário é obrigatório.")
    @Size(max = 300, message = "O comentário deve ter no máximo 300 caracteres.")
    private String comentario;

    private StatusOcorrencia status;
    private LocalDateTime dataRegistro;

    public Acompanhamento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdOcorrencia() { return idOcorrencia; }
    public void setIdOcorrencia(Long idOcorrencia) { this.idOcorrencia = idOcorrencia; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
}
