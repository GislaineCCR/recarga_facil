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
            // Reutiliza usuário existente com o mesmo e-mail ou cria novo
            usuario = usuarioRepository.findFirstByEmail(dto.getEmail())
                    .orElseGet(() -> {
                        Usuario novoUsuario = new Usuario();
                        novoUsuario.setNome(dto.getNome());
                        novoUsuario.setEmail(dto.getEmail());
                        novoUsuario.setSenha(dto.getSenha() != null ? dto.getSenha() : "123456");
                        return usuarioRepository.save(novoUsuario);
                    });
            if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                usuario.setSenha(dto.getSenha());
                usuarioRepository.save(usuario);
            }
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
        // 1. Reutiliza ou cria usuário
        Usuario usuario = usuarioRepository.findFirstByEmail(dto.getEmail())
                .orElseGet(() -> {
                    Usuario novo = new Usuario();
                    novo.setNome(dto.getNome());
                    novo.setEmail(dto.getEmail());
                    novo.setSenha(dto.getSenha() != null && !dto.getSenha().isBlank() ? dto.getSenha() : "123456");
                    return usuarioRepository.save(novo);
                });
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(dto.getSenha());
            usuarioRepository.save(usuario);
        }

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

    public Motorista atualizar(Long id, MotoristaDTO dto) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com ID: " + id));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            motorista.setNome(dto.getNome());
            if (motorista.getUsuario() != null) {
                motorista.getUsuario().setNome(dto.getNome());
                usuarioRepository.save(motorista.getUsuario());
            }
        }
        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            motorista.setTelefone(dto.getTelefone());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            motorista.setEmail(dto.getEmail());
            if (motorista.getUsuario() != null) {
                motorista.getUsuario().setEmail(dto.getEmail());
                usuarioRepository.save(motorista.getUsuario());
            }
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank() && motorista.getUsuario() != null) {
            motorista.getUsuario().setSenha(dto.getSenha());
            usuarioRepository.save(motorista.getUsuario());
        }

        return motoristaRepository.save(motorista);
    }
}
