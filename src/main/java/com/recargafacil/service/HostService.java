package com.recargafacil.service;

import com.recargafacil.dto.CadastroHostDTO;
import com.recargafacil.dto.HostDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Local;
import com.recargafacil.model.Usuario;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.UsuarioRepository;
import com.recargafacil.repository.WallboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HostService {

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private WallboxRepository wallboxRepository;

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

        Host host = new Host();
        host.setUsuario(usuario);
        host.setNome(dto.getNome());
        host.setEmail(dto.getEmail());
        host.setDadosBancarios(dto.getDadosBancarios());

        return hostRepository.save(host);
    }

    public Host cadastrarComWallbox(CadastroHostDTO dto) {
        // 1. Reutiliza ou cria Usuario
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

        // 2. Cria Host
        Host host = new Host();
        host.setUsuario(usuario);
        host.setNome(dto.getNome());
        host.setEmail(dto.getEmail());
        host.setDadosBancarios(dto.getDadosBancarios());
        host.setTelefone(dto.getTelefone());
        host.setCpf(dto.getCpf());
        host = hostRepository.save(host);

        // 3. Cria Local e Wallbox se informado
        if (dto.getModelo() != null && !dto.getModelo().isBlank()) {
            Local local = new Local();
            local.setEnderecoCompleto(dto.getEnderecoCompleto() != null ? dto.getEnderecoCompleto() : "Endereço não informado");
            local.setLatitude(dto.getLatitude() != null ? dto.getLatitude() : -23.5100);
            local.setLongitude(dto.getLongitude() != null ? dto.getLongitude() : -47.4600);
            local.setTipoAmbiente(dto.getTipoAmbiente() != null ? dto.getTipoAmbiente() : Local.TipoAmbiente.RESIDENCIAL);
            local.setInstrucoesAcesso(dto.getInstrucoesAcesso());
            local.setAcessoEnderecoAposPagamento(dto.getAcessoEnderecoAposPagamento() != null ? dto.getAcessoEnderecoAposPagamento() : true);
            local.setPossuiVagaEstacionamento(dto.getPossuiVagaEstacionamento() != null ? dto.getPossuiVagaEstacionamento() : true);
            local.setTipoVagaEstacionamento(dto.getTipoVagaEstacionamento());
            local.setAutorizacaoCondominio(dto.getAutorizacaoCondominio() != null ? dto.getAutorizacaoCondominio() : true);
            local.setDetalhesVaga(dto.getDetalhesVaga());

            Wallbox wallbox = new Wallbox();
            wallbox.setHost(host);
            wallbox.setLocal(local);
            wallbox.setModelo(dto.getModelo());
            wallbox.setPotencia(dto.getPotencia() != null ? dto.getPotencia() : Wallbox.Potencia.P_7KW);
            wallbox.setStatus(dto.getStatus() != null ? dto.getStatus() : Wallbox.Status.DISPONIVEL);
            wallbox.setPrecoHora(dto.getPrecoHora() != null ? dto.getPrecoHora() : new BigDecimal("15.00"));
            wallbox.setComodidades(dto.getComodidades() != null && !dto.getComodidades().isBlank() ? dto.getComodidades() : "wifi,coberto");
            wallbox.setFotoUrl(dto.getFotoUrl() != null && !dto.getFotoUrl().isBlank() ? dto.getFotoUrl() : "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80");

            wallboxRepository.save(wallbox);
        }

        return host;
    }

    public List<Host> listar() {
        return hostRepository.findAll();
    }

    public Optional<Host> buscarPorId(Long id) {
        return hostRepository.findById(id);
    }

    public Host atualizar(Long id, HostDTO dto) {
        Host host = hostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Host não encontrado com ID: " + id));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            host.setNome(dto.getNome());
            if (host.getUsuario() != null) {
                host.getUsuario().setNome(dto.getNome());
                usuarioRepository.save(host.getUsuario());
            }
        }
        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            host.setTelefone(dto.getTelefone());
        }
        if (dto.getCpf() != null && !dto.getCpf().isBlank()) {
            host.setCpf(dto.getCpf());
        }
        if (dto.getDadosBancarios() != null && !dto.getDadosBancarios().isBlank()) {
            host.setDadosBancarios(dto.getDadosBancarios());
        }
        if (dto.getFotoUrl() != null && !dto.getFotoUrl().isBlank()) {
            host.setFotoUrl(dto.getFotoUrl());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            host.setEmail(dto.getEmail());
            if (host.getUsuario() != null) {
                host.getUsuario().setEmail(dto.getEmail());
                usuarioRepository.save(host.getUsuario());
            }
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank() && host.getUsuario() != null) {
            host.getUsuario().setSenha(dto.getSenha());
            usuarioRepository.save(host.getUsuario());
        }

        return hostRepository.save(host);
    }
}
