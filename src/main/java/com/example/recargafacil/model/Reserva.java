package com.example.recargafacil.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_motorista")
    private Motorista motorista;

    @ManyToOne
    @JoinColumn(name = "id_veiculo")
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "id_wallbox")
    private Wallbox wallbox;

    private LocalDate dataReserva;
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    private StatusReserva statusReserva = StatusReserva.PENDENTE;

    public enum StatusReserva {
        PENDENTE, CONCLUIDA, NO_SHOW, CANCELADA
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Motorista getMotorista() { return motorista; }
    public void setMotorista(Motorista motorista) { this.motorista = motorista; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public Wallbox getWallbox() { return wallbox; }
    public void setWallbox(Wallbox wallbox) { this.wallbox = wallbox; }
    public LocalDate getDataReserva() { return dataReserva; }
    public void setDataReserva(LocalDate dataReserva) { this.dataReserva = dataReserva; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public StatusReserva getStatusReserva() { return statusReserva; }
    public void setStatusReserva(StatusReserva statusReserva) { this.statusReserva = statusReserva; }
}