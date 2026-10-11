package com.recargafacil.controller;

import com.recargafacil.dto.ReservaDTO;
import com.recargafacil.dto.StatusUpdateDTO;
import com.recargafacil.model.Reserva;
import com.recargafacil.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservation Controller", description = "Endpoints para gerenciamento de agendamentos de recarga")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    @Operation(summary = "RF07: Agendar uma nova reserva de recarga")
    public ResponseEntity<Reserva> agendar(@RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(reservaService.agendar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todas as reservas")
    public ResponseEntity<List<Reserva>> listar() {
        return ResponseEntity.ok(reservaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por ID")
    public ResponseEntity<Reserva> buscarPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "RF09: Atualizar status da reserva (ex: PENDENTE para CONCLUIDA)")
    public ResponseEntity<Reserva> atualizarStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        Reserva.StatusReserva status = Reserva.StatusReserva.valueOf(dto.getStatus().toUpperCase());
        return ResponseEntity.ok(reservaService.atualizarStatus(id, status));
    }
}
