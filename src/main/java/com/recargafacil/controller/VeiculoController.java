package com.recargafacil.controller;

import com.recargafacil.dto.VeiculoDTO;
import com.recargafacil.model.Veiculo;
import com.recargafacil.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@Tag(name = "Veiculo Controller", description = "Endpoints para gerenciamento de veículos (RF02)")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @PostMapping
    @Operation(summary = "RF02: Cadastrar veículo vinculando ao motorista")
    public ResponseEntity<Veiculo> cadastrar(@RequestBody VeiculoDTO dto) {
        return ResponseEntity.ok(veiculoService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos")
    public ResponseEntity<List<Veiculo>> listar() {
        return ResponseEntity.ok(veiculoService.listar());
    }

    @GetMapping("/motorista/{idMotorista}")
    @Operation(summary = "Listar veículos de um motorista específico")
    public ResponseEntity<List<Veiculo>> buscarPorMotorista(@PathVariable Long idMotorista) {
        return ResponseEntity.ok(veiculoService.buscarPorMotorista(idMotorista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable Long id) {
        return veiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
