package com.recargafacil.service;

import com.recargafacil.dto.VeiculoDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Veiculo;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    /**
     * RF02: Cadastrar Veículo
     * Inserir dados do carro (modelo, placa, conector) na tabela Veículo, vinculando ao ID do motorista.
     */
    public Veiculo cadastrar(VeiculoDTO dto) {
        Motorista motorista = motoristaRepository.findById(dto.getIdMotorista())
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com ID: " + dto.getIdMotorista()));

        Veiculo veiculo = new Veiculo();
        veiculo.setMotorista(motorista);
        veiculo.setModelo(dto.getModelo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setPlaca(dto.getPlaca());
        veiculo.setRenavan(dto.getRenavan());
        veiculo.setTipoConector(dto.getTipoConector());

        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listar() {
        return veiculoRepository.findAll();
    }

    public List<Veiculo> buscarPorMotorista(Long idMotorista) {
        return veiculoRepository.findByMotoristaId(idMotorista);
    }

    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoRepository.findById(id);
    }
}
