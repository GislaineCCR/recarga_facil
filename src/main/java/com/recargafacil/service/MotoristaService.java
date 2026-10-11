package com.recargafacil.service;

import com.recargafacil.dto.MotoristaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Usuario;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MotoristaService {

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * RF01: Cadastrar Motorista
     * Insere novo registro de usuário e vincula à tabela Motorista com dados pessoais.
     */
    public Motorista cadastrar(MotoristaDTO dto) {
        Usuario usuario;

        if (dto.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + dto.getIdUsuario()));
        } else {
            // Criação automática do registro base de Usuario caso não tenha ID prévio
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(dto.getNome());
            novoUsuario.setEmail(dto.getEmail());
            novoUsuario.setSenha(dto.getSenha() != null ? dto.getSenha() : "123456");
            usuario = usuarioRepository.save(novoUsuario);
        }

        Motorista motorista = new Motorista();
        motorista.setUsuario(usuario);
        motorista.setNome(dto.getNome());
        motorista.setCpf(dto.getCpf());
        motorista.setEmail(dto.getEmail());
        motorista.setTelefone(dto.getTelefone());

        return motoristaRepository.save(motorista);
    }

    public List<Motorista> listar() {
        return motoristaRepository.findAll();
    }

    public Optional<Motorista> buscarPorId(Long id) {
        return motoristaRepository.findById(id);
    }
}
