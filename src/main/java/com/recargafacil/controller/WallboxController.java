package com.recargafacil.controller;

import com.recargafacil.dto.WallboxDTO;
import com.recargafacil.model.Wallbox;
import com.recargafacil.service.WallboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallboxes")
@Tag(name = "Wallbox Controller", description = "Endpoints para gerenciamento e busca de pontos de recarga")
public class WallboxController {

    @Autowired
    private WallboxService wallboxService;

    @PostMapping
    @Operation(summary = "RF04: Cadastrar uma nova Wallbox no sistema")
    public ResponseEntity<Wallbox> cadastrar(@RequestBody WallboxDTO dto) {
        return ResponseEntity.ok(wallboxService.cadastrar(dto));
    }

    @GetMapping("/nearby")
    @Operation(summary = "RF06: Buscar pontos de recarga próximos via coordenadas GPS e raio em KM")
    public ResponseEntity<List<Wallbox>> buscarProximos(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "15.0") Double radius) {
        return ResponseEntity.ok(wallboxService.buscarProximos(lat, lng, radius));
    }

    @GetMapping
    @Operation(summary = "RF06: Listar todas as Wallboxes cadastradas")
    public ResponseEntity<List<Wallbox>> listar() {
        return ResponseEntity.ok(wallboxService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de uma Wallbox pelo ID")
    public ResponseEntity<Wallbox> buscarPorId(@PathVariable Long id) {
        return wallboxService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}