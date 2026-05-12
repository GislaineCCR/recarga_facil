package com.example.recargafacil.model;

import jakarta.persistence.*;

@Entity
public class Wallbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_host")
    private Host host;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_local")
    private Local local;

    private String modelo;

    @Enumerated(EnumType.STRING)
    private Potencia potencia;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DISPONIVEL;

    public enum Potencia {
        P_7KW("7kW"), P_11KW("11kW"), P_22KW("22kW");
        private String value;
        Potencia(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public enum Status {
        DISPONIVEL, OCUPADO, MANUTENCAO, INDISPONIVEL
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Host getHost() { return host; }
    public void setHost(Host host) { this.host = host; }
    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Potencia getPotencia() { return potencia; }
    public void setPotencia(Potencia potencia) { this.potencia = potencia; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}