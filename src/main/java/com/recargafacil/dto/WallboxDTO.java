package com.recargafacil.dto;

import com.recargafacil.model.Local.TipoAmbiente;
import com.recargafacil.model.Wallbox.Potencia;
import com.recargafacil.model.Wallbox.Status;
import java.math.BigDecimal;

public class WallboxDTO {
    private Long idHost;
    private Double latitude;
    private Double longitude;
    private String enderecoCompleto;
    private TipoAmbiente tipoAmbiente;
    private String instrucoesAcesso;
    private Boolean acessoEnderecoAposPagamento;
    private String modelo;
    private Potencia potencia;
    private BigDecimal precoHora;
    private String comodidades;
    private String fotoUrl;
    private Status status;
    private Boolean possuiVagaEstacionamento;
    private String tipoVagaEstacionamento;
    private Boolean autorizacaoCondominio;
    private String detalhesVaga;

    // Getters and Setters
    public Long getIdHost() { return idHost; }
    public void setIdHost(Long idHost) { this.idHost = idHost; }

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

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Potencia getPotencia() { return potencia; }
    public void setPotencia(Potencia potencia) { this.potencia = potencia; }

    public BigDecimal getPrecoHora() { return precoHora; }
    public void setPrecoHora(BigDecimal precoHora) { this.precoHora = precoHora; }

    public String getComodidades() { return comodidades; }
    public void setComodidades(String comodidades) { this.comodidades = comodidades; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Boolean getPossuiVagaEstacionamento() { return possuiVagaEstacionamento; }
    public void setPossuiVagaEstacionamento(Boolean possuiVagaEstacionamento) { this.possuiVagaEstacionamento = possuiVagaEstacionamento; }

    public String getTipoVagaEstacionamento() { return tipoVagaEstacionamento; }
    public void setTipoVagaEstacionamento(String tipoVagaEstacionamento) { this.tipoVagaEstacionamento = tipoVagaEstacionamento; }

    public Boolean getAutorizacaoCondominio() { return autorizacaoCondominio; }
    public void setAutorizacaoCondominio(Boolean autorizacaoCondominio) { this.autorizacaoCondominio = autorizacaoCondominio; }

    public String getDetalhesVaga() { return detalhesVaga; }
    public void setDetalhesVaga(String detalhesVaga) { this.detalhesVaga = detalhesVaga; }
}