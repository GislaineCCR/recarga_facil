package com.recargafacil.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaDTO {
    private Long idMotorista;
    private Long idWallbox;
    private Long idVeiculo;
    private LocalDate dataReserva;
    private LocalTime horario;

    // Getters and Setters
    public Long getIdMotorista() { return idMotorista; }
    public void setIdMotorista(Long idMotorista) { this.idMotorista = idMotorista; }
    public Long getIdWallbox() { return idWallbox; }
    public void setIdWallbox(Long idWallbox) { this.idWallbox = idWallbox; }
    public Long getIdVeiculo() { return idVeiculo; }
    public void setIdVeiculo(Long idVeiculo) { this.idVeiculo = idVeiculo; }
    public LocalDate getDataReserva() { return dataReserva; }
    public void setDataReserva(LocalDate dataReserva) { this.dataReserva = dataReserva; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
}