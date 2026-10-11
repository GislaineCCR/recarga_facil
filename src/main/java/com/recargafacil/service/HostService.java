package com.recargafacil.service;

import com.recargafacil.dto.HostDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Usuario;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class HostService {

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * RF03: Cadastrar Anfitrião
     * Inserir dados pessoais e bancários do proprietário na tabela Host.
     */
    public Host cadastrar(HostDTO dto) {
        Usuario usuario;

        if (dto.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + dto.getIdUsuario()));
        } else {
            // Criação automática do registro de Usuario
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(dto.getNome());
            novoUsuario.setEmail(dto.getEmail());
            novoUsuario.setSenha(dto.getSenha() != null ? dto.getSenha() : "123456");
            usuario = usuarioRepository.save(novoUsuario);
        }

        Host host = new Host();
        host.setUsuario(usuario);
        host.setNome(dto.getNome());
        host.setEmail(dto.getEmail());
        host.setDadosBancarios(dto.getDadosBancarios());

        return hostRepository.save(host);
    }

    public List<Host> listar() {
        return hostRepository.findAll();
    }

    public Optional<Host> buscarPorId(Long id) {
        return hostRepository.findById(id);
    }
}
