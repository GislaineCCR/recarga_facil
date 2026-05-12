package com.example.recargafacil.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;
    private String enderecoCompleto;

    @Enumerated(EnumType.STRING)
    private TipoAmbiente tipoAmbiente;

    @Column(columnDefinition = "TEXT")
    private String instrucoesAcesso;

    private Boolean acessoEnderecoAposPagamento = true;

    public enum TipoAmbiente {
        RESIDENCIAL, COMERCIAL
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getEnderecoCompleto() { return enderecoCompleto; }
    public void setEnderecoCompleto(String enderecoCompleto) { this.enderecoCompleto = enderecoCompleto; }
    public TipoAmbiente getTipoAmbiente() { return tipoAmbiente; }
    public void setTipoAmbiente(TipoAmbiente tipoAmbiente) { this.tipoAmbiente = tipoAmbiente; }
    public String getInstrucoesAcesso() { return instrucoesAcesso; }
    public void setInstrucoesAcesso(String instrucoesAcesso) { this.instrucoesAcesso = instrucoesAcesso; }
    public Boolean getAcessoEnderecoAposPagamento() { return acessoEnderecoAposPagamento; }
    public void setAcessoEnderecoAposPagamento(Boolean acessoEnderecoAposPagamento) { this.acessoEnderecoAposPagamento = acessoEnderecoAposPagamento; }
}