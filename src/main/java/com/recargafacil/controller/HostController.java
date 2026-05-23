package com.recargafacil.controller;

import com.recargafacil.dto.HostDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Usuario;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hosts")
public class HostController {

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Host> cadastrar(@RequestBody HostDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Host host = new Host();
        host.setUsuario(usuario);
        host.setNome(dto.getNome());
        host.setEmail(dto.getEmail());
        host.setDadosBancarios(dto.getDadosBancarios());

        return ResponseEntity.ok(hostRepository.save(host));
    }

    @GetMapping
    public ResponseEntity<List<Host>> listar() {
        return ResponseEntity.ok(hostRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Host> buscarPorId(@PathVariable Long id) {
        return hostRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}