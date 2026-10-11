package com.recargafacil.dto;

import com.recargafacil.model.Agenda.DiaSemana;
import java.time.LocalTime;

public class AgendaDTO {
    private Long idWallbox;
    private DiaSemana diaSemana;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private Boolean estaAtivo = true;

    // Getters and Setters
    public Long getIdWallbox() { return idWallbox; }
    public void setIdWallbox(Long idWallbox) { this.idWallbox = idWallbox; }
    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(LocalTime horarioInicio) { this.horarioInicio = horarioInicio; }
    public LocalTime getHorarioFim() { return horarioFim; }
    public void setHorarioFim(LocalTime horarioFim) { this.horarioFim = horarioFim; }
    public Boolean getEstaAtivo() { return estaAtivo; }
    public void setEstaAtivo(Boolean estaAtivo) { this.estaAtivo = estaAtivo; }
}
