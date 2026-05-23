package com.example.recargafacil.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_wallbox")
    private Wallbox wallbox;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;

    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private Boolean estaAtivo = true;

    public enum DiaSemana {
        DOMINGO, SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Wallbox getWallbox() { return wallbox; }
    public void setWallbox(Wallbox wallbox) { this.wallbox = wallbox; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(LocalTime horarioInicio) { this.horarioInicio = horarioInicio; }
    public LocalTime getHorarioFim() { return horarioFim; }
    public void setHorarioFim(LocalTime horarioFim) { this.horarioFim = horarioFim; }
    public Boolean getEstaAtivo() { return estaAtivo; }
    public void setEstaAtivo(Boolean estaAtivo) { this.estaAtivo = estaAtivo; }
}