package com.example.recargafacil.controller;

import com.example.recargafacil.dto.ReservaDTO;
import com.example.recargafacil.model.Motorista;
import com.example.recargafacil.model.Reserva;
import com.example.recargafacil.model.Wallbox;
import com.example.recargafacil.repository.MotoristaRepository;
import com.example.recargafacil.repository.ReservaRepository;
import com.example.recargafacil.repository.WallboxRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservation Controller", description = "Endpoints for managing EV charging reservations")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private WallboxRepository wallboxRepository;

    @PostMapping
    @Operation(summary = "Schedule a new reservation")
    public ResponseEntity<Reserva> agendar(@RequestBody ReservaDTO dto) {
        Motorista motorista = motoristaRepository.findById(dto.getIdMotorista())
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado"));

        Wallbox wallbox = wallboxRepository.findById(dto.getIdWallbox())
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada"));

        Reserva reserva = new Reserva();
        reserva.setMotorista(motorista);
        reserva.setWallbox(wallbox);
        reserva.setDataReserva(dto.getDataReserva());
        reserva.setHorario(dto.getHorario());

        return ResponseEntity.ok(reservaRepository.save(reserva));
    }

    @GetMapping
    @Operation(summary = "List all reservations")
    public ResponseEntity<List<Reserva>> listar() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }

    @GetMapping("/wallbox/{wallboxId}")
    @Operation(summary = "List reservations by Wallbox ID")
    public ResponseEntity<List<Reserva>> listarPorWallbox(@PathVariable Long wallboxId) {
        return ResponseEntity.ok(reservaRepository.findByWallboxId(wallboxId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find reservation by ID")
    public ResponseEntity<Reserva> buscarPorId(@PathVariable Long id) {
        return reservaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
