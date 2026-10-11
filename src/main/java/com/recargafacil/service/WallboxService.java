package com.recargafacil.service;

import com.recargafacil.dto.WallboxDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Local;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.WallboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WallboxService {

    @Autowired
    private WallboxRepository wallboxRepository;

    @Autowired
    private HostRepository hostRepository;

    /**
     * RF04: Cadastrar Ponto de Recarga
     * Gravar o endereço na tabela Local e vincular os dados técnicos do equipamento na tabela Wallbox.
     */
    public Wallbox cadastrar(WallboxDTO dto) {
        Host host = hostRepository.findById(dto.getIdHost())
                .orElseThrow(() -> new RuntimeException("Host não encontrado com ID: " + dto.getIdHost()));

        Local local = new Local();
        local.setLatitude(dto.getLatitude());
        local.setLongitude(dto.getLongitude());
        local.setEnderecoCompleto(dto.getEnderecoCompleto());
        local.setTipoAmbiente(dto.getTipoAmbiente());
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
        wallbox.setPotencia(dto.getPotencia());
        if (dto.getPrecoHora() != null) {
            wallbox.setPrecoHora(dto.getPrecoHora());
        }
        if (dto.getComodidades() != null) {
            wallbox.setComodidades(dto.getComodidades());
        }
        if (dto.getFotoUrl() != null) {
            wallbox.setFotoUrl(dto.getFotoUrl());
        }
        if (dto.getStatus() != null) {
            wallbox.setStatus(dto.getStatus());
        }

        return wallboxRepository.save(wallbox);
    }

    /**
     * RF06: Buscar Estações
     * Realizar leitura (Select) nas tabelas Wallbox e Local para exibir os pontos disponíveis.
     */
    public List<Wallbox> listar() {
        return wallboxRepository.findAll();
    }

    public List<Wallbox> buscarProximos(Double lat, Double lng, Double radius) {
        return wallboxRepository.findNearby(lat, lng, radius);
    }

    public List<Wallbox> listarPorHost(Long hostId) {
        return wallboxRepository.findByHostId(hostId);
    }

    public Optional<Wallbox> buscarPorId(Long id) {
        return wallboxRepository.findById(id);
    }

    public Wallbox atualizarStatus(Long id, Wallbox.Status status) {
        Wallbox wb = wallboxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada com ID: " + id));
        wb.setStatus(status);
        return wallboxRepository.save(wb);
    }
}
