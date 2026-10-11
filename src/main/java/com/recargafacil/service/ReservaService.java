package com.recargafacil.service;

import com.recargafacil.dto.ReservaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Reserva;
import com.recargafacil.model.Veiculo;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.ReservaRepository;
import com.recargafacil.repository.VeiculoRepository;
import com.recargafacil.repository.WallboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private WallboxRepository wallboxRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    /**
     * RF07: Registrar Reserva
     * Inserir o agendamento na tabela Reserva, vinculando as chaves estrangeiras de Motorista, Veículo e Wallbox.
     */
    public Reserva agendar(ReservaDTO dto) {
        Long idMotorista = (dto.getIdMotorista() != null) ? dto.getIdMotorista() : 1L;
        Motorista motorista = motoristaRepository.findById(idMotorista)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com ID: " + idMotorista));

        Wallbox wallbox = wallboxRepository.findById(dto.getIdWallbox())
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada com ID: " + dto.getIdWallbox()));

        Veiculo veiculo = null;
        if (dto.getIdVeiculo() != null) {
            veiculo = veiculoRepository.findById(dto.getIdVeiculo()).orElse(null);
        }

        Reserva reserva = new Reserva();
        reserva.setMotorista(motorista);
        reserva.setWallbox(wallbox);
        reserva.setVeiculo(veiculo);
        reserva.setDataReserva(dto.getDataReserva());
        reserva.setHorario(dto.getHorario());
        reserva.setStatusReserva(Reserva.StatusReserva.PENDENTE);

        return reservaRepository.save(reserva);
    }

    /**
     * RF09: Atualizar Status da Reserva
     * Atualizar o campo status_reserva (PENDENTE, CONCLUIDA, CANCELADA, NO_SHOW).
     */
    public Reserva atualizarStatus(Long id, Reserva.StatusReserva novoStatus) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada com ID: " + id));

        reserva.setStatusReserva(novoStatus);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> listarPorHost(Long hostId) {
        return reservaRepository.findByWallboxHostId(hostId);
    }

    public List<Reserva> listarPorMotorista(Long motoristaId) {
        return reservaRepository.findByMotoristaId(motoristaId);
    }

    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }
}
