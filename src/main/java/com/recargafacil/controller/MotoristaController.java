package com.recargafacil.controller;

import com.recargafacil.dto.MotoristaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Usuario;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Motorista> cadastrar(@RequestBody MotoristaDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Motorista motorista = new Motorista();
        motorista.setUsuario(usuario);
        motorista.setNome(dto.getNome());
        motorista.setCpf(dto.getCpf());
        motorista.setEmail(dto.getEmail());
        motorista.setTelefone(dto.getTelefone());

        return ResponseEntity.ok(motoristaRepository.save(motorista));
    }

    @GetMapping
    public ResponseEntity<List<Motorista>> listar() {
        return ResponseEntity.ok(motoristaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Motorista> buscarPorId(@PathVariable Long id) {
        return motoristaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}