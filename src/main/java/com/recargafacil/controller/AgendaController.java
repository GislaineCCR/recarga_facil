package com.recargafacil.controller;

import com.recargafacil.dto.AgendaDTO;
import com.recargafacil.model.Agenda;
import com.recargafacil.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agendas")
@Tag(name = "Agenda Controller", description = "Endpoints para gerenciamento de disponibilidade (RF05)")
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @PostMapping
    @Operation(summary = "RF05: Inserir novo dia e horário de disponibilidade para uma Wallbox")
    public ResponseEntity<Agenda> cadastrar(@RequestBody AgendaDTO dto) {
        return ResponseEntity.ok(agendaService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todas as agendas cadastradas")
    public ResponseEntity<List<Agenda>> listar() {
        return ResponseEntity.ok(agendaService.listar());
    }

    @GetMapping("/wallbox/{idWallbox}")
    @Operation(summary = "Listar disponibilidade de uma Wallbox específica")
    public ResponseEntity<List<Agenda>> listarPorWallbox(@PathVariable Long idWallbox) {
        return ResponseEntity.ok(agendaService.listarPorWallbox(idWallbox));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um horário da agenda")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        agendaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/wallbox/{idWallbox}/grade")
    @Operation(summary = "Salvar ou atualizar a grade semanal de disponibilidade da Wallbox")
    public ResponseEntity<List<Agenda>> salvarGrade(@PathVariable Long idWallbox, @RequestBody List<AgendaDTO> grade) {
        return ResponseEntity.ok(agendaService.salvarGrade(idWallbox, grade));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Alternar status ativo/inativo de um dia da agenda")
    public ResponseEntity<Agenda> alternarStatusDia(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.alternarStatusDia(id));
    }
}
