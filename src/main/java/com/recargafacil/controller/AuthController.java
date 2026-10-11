package com.recargafacil.controller;

import com.recargafacil.dto.LoginRequest;
import com.recargafacil.dto.MotoristaDTO;
import com.recargafacil.dto.HostDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Usuario;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.UsuarioRepository;
import com.recargafacil.service.HostService;
import com.recargafacil.service.MotoristaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "Autenticação separada para Anfitriões (Hosts) e Motoristas")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private HostService hostService;

    @Autowired
    private MotoristaService motoristaService;

    @PostMapping("/login-host")
    @Operation(summary = "Login exclusivo para Anfitrião (Host)")
    public ResponseEntity<?> loginHost(@RequestBody LoginRequest login) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndSenha(login.getEmail(), login.getSenha());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "E-mail ou senha incorretos."));
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Host> hostOpt = hostRepository.findByUsuarioId(usuario.getId());
        if (hostOpt.isEmpty()) {
            hostOpt = hostRepository.findByEmail(usuario.getEmail());
        }

        if (hostOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Este usuário não possui perfil de Anfitrião (Host) cadastrado."));
        }

        Host host = hostOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "HOST");
        response.put("id", host.getId());
        response.put("nome", host.getNome());
        response.put("email", host.getEmail());
        response.put("dadosBancarios", host.getDadosBancarios());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login-motorista")
    @Operation(summary = "Login exclusivo para Cliente / Motorista")
    public ResponseEntity<?> loginMotorista(@RequestBody LoginRequest login) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndSenha(login.getEmail(), login.getSenha());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "E-mail ou senha incorretos."));
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Motorista> motoristaOpt = motoristaRepository.findByUsuarioId(usuario.getId());
        if (motoristaOpt.isEmpty()) {
            motoristaOpt = motoristaRepository.findByEmail(usuario.getEmail());
        }

        if (motoristaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Este usuário não possui perfil de Motorista cadastrado."));
        }

        Motorista motorista = motoristaOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "MOTORISTA");
        response.put("id", motorista.getId());
        response.put("nome", motorista.getNome());
        response.put("email", motorista.getEmail());
        response.put("telefone", motorista.getTelefone());
        response.put("cpf", motorista.getCpf());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro-motorista")
    @Operation(summary = "Cadastro rápido de novo Motorista")
    public ResponseEntity<?> registrarMotorista(@RequestBody MotoristaDTO dto) {
        try {
            Motorista motorista = motoristaService.cadastrar(dto);
            return ResponseEntity.ok(motorista);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registro-host")
    @Operation(summary = "Cadastro rápido de novo Host")
    public ResponseEntity<?> registrarHost(@RequestBody HostDTO dto) {
        try {
            Host host = hostService.cadastrar(dto);
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
