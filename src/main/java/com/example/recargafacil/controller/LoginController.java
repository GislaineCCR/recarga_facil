package com.example.recargafacil.controller;

import com.example.recargafacil.dto.LoginRequest;
import com.example.recargafacil.model.Usuario;
import com.example.recargafacil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            if (usuario.getSenha().equals(loginRequest.getSenha())) {
                return ResponseEntity.ok("Login bem-sucedido!");
            }
        }

        return ResponseEntity.status(401).body("Email ou senha inválidos.");
    }
}