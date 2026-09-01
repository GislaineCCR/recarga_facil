package com.recargafacil.model;

import jakarta.persistence.*;

@Entity
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_motorista")
    private Motorista motorista;

    private String modelo;
    private String marca;
    private String placa;
    private String renavan;

    @Enumerated(EnumType.STRING)
    private TipoConector tipoConector;

    public enum TipoConector {
        TIPO_1, TIPO_2, CCS2, GB_T
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Motorista getMotorista() { return motorista; }
    public void setMotorista(Motorista motorista) { this.motorista = motorista; }
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