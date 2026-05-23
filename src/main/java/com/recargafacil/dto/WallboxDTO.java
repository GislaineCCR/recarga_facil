package com.recargafacil.dto;

import com.recargafacil.model.Local.TipoAmbiente;
import com.recargafacil.model.Wallbox.Potencia;

public class WallboxDTO {
    private Long idHost;
    private Double latitude;
    private Double longitude;
    private String enderecoCompleto;
    private TipoAmbiente tipoAmbiente;
    private String instrucoesAcesso;
    private String modelo;
    private Potencia potencia;

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
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Potencia getPotencia() { return potencia; }
    public void setPotencia(Potencia potencia) { this.potencia = potencia; }
}