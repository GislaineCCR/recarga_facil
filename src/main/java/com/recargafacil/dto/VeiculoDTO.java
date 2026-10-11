package com.recargafacil.dto;

import com.recargafacil.model.Veiculo.TipoConector;

public class VeiculoDTO {
    private Long idMotorista;
    private String modelo;
    private String marca;
    private String placa;
    private String renavan;
    private TipoConector tipoConector;

    // Getters and Setters
    public Long getIdMotorista() { return idMotorista; }
    public void setIdMotorista(Long idMotorista) { this.idMotorista = idMotorista; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getRenavan() { return renavan; }
    public void setRenavan(String renavan) { this.renavan = renavan; }
    public TipoConector getTipoConector() { return tipoConector; }
    public void setTipoConector(TipoConector tipoConector) { this.tipoConector = tipoConector; }
}
