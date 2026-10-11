package com.recargafacil.controller;

import com.recargafacil.dto.MotoristaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.service.MotoristaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
@Tag(name = "Motorista Controller", description = "Endpoints para gerenciamento de motoristas (RF01)")
public class MotoristaController {

    @Autowired
    private MotoristaService motoristaService;

    @PostMapping
    @Operation(summary = "RF01: Cadastrar novo motorista com dados pessoais e credenciais")
    public ResponseEntity<Motorista> cadastrar(@RequestBody MotoristaDTO dto) {
        return ResponseEntity.ok(motoristaService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os motoristas")
    public ResponseEntity<List<Motorista>> listar() {
        return ResponseEntity.ok(motoristaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar motorista por ID")
    public ResponseEntity<Motorista> buscarPorId(@PathVariable Long id) {
        return motoristaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados cadastrais do motorista (telefone, nome, etc.)")
    public ResponseEntity<Motorista> atualizar(@PathVariable Long id, @RequestBody MotoristaDTO dto) {
        return ResponseEntity.ok(motoristaService.atualizar(id, dto));
    }
}