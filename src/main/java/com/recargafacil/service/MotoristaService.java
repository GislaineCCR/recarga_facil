package com.recargafacil.service;

import com.recargafacil.dto.CadastroMotoristaDTO;
import com.recargafacil.dto.MotoristaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Usuario;
import com.recargafacil.model.Veiculo;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.UsuarioRepository;
import com.recargafacil.repository.VeiculoRepository;
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

    @Autowired
    private VeiculoRepository veiculoRepository;

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

    public Motorista cadastrarComVeiculo(CadastroMotoristaDTO dto) {
        // 1. Cria usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha() != null && !dto.getSenha().isBlank() ? dto.getSenha() : "123456");
        usuario = usuarioRepository.save(usuario);

        // 2. Cria motorista
        Motorista motorista = new Motorista();
        motorista.setUsuario(usuario);
        motorista.setNome(dto.getNome());
        motorista.setCpf(dto.getCpf());
        motorista.setEmail(dto.getEmail());
        motorista.setTelefone(dto.getTelefone());
        motorista = motoristaRepository.save(motorista);

        // 3. Cria veículo se informado
        if (dto.getModelo() != null && !dto.getModelo().isBlank()) {
            Veiculo veiculo = new Veiculo();
            veiculo.setMotorista(motorista);
            veiculo.setMarca(dto.getMarca() != null ? dto.getMarca() : "EV");
            veiculo.setModelo(dto.getModelo());
            veiculo.setPlaca(dto.getPlaca() != null ? dto.getPlaca().toUpperCase() : "SEM-PLACA");
            veiculo.setRenavan(dto.getRenavan());
            veiculo.setTipoConector(dto.getTipoConector() != null ? dto.getTipoConector() : Veiculo.TipoConector.TIPO_2);
            veiculoRepository.save(veiculo);
        }

        return motorista;
    }

    public List<Motorista> listar() {
        return motoristaRepository.findAll();
    }

    public Optional<Motorista> buscarPorId(Long id) {
        return motoristaRepository.findById(id);
    }
}
