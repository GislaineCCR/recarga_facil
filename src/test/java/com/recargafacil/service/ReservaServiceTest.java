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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ReservaService (RF07 e RF09)")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private MotoristaRepository motoristaRepository;

    @Mock
    private WallboxRepository wallboxRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    @DisplayName("CT06 - Deve agendar reserva com status inicial PENDENTE vinculando Motorista, Wallbox e Veículo (RF07)")
    void deveAgendarReservaComSucesso() {
        Motorista motorista = new Motorista();
        motorista.setId(1L);
        motorista.setNome("Gislaine Rodrigues");

        Wallbox wallbox = new Wallbox();
        wallbox.setId(2L);
        wallbox.setModelo("Wallbox EcoCharge 7.4kW");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(3L);
        veiculo.setModelo("BYD Song Plus");

        ReservaDTO dto = new ReservaDTO();
        dto.setIdMotorista(1L);
        dto.setIdWallbox(2L);
        dto.setIdVeiculo(3L);
        dto.setDataReserva(LocalDate.of(2026, 10, 25));
        dto.setHorario(LocalTime.of(14, 30));

        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motorista));
        when(wallboxRepository.findById(2L)).thenReturn(Optional.of(wallbox));
        when(veiculoRepository.findById(3L)).thenReturn(Optional.of(veiculo));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaService.agendar(dto);

        assertNotNull(reserva);
        assertEquals(Reserva.StatusReserva.PENDENTE, reserva.getStatusReserva());
        assertEquals(motorista, reserva.getMotorista());
        assertEquals(wallbox, reserva.getWallbox());
        assertEquals(veiculo, reserva.getVeiculo());
        assertEquals(LocalDate.of(2026, 10, 25), reserva.getDataReserva());
        assertEquals(LocalTime.of(14, 30), reserva.getHorario());
    }

    @Test
    @DisplayName("CT07 - Deve lançar exceção ao agendar reserva com Motorista inexistente (RF07)")
    void deveLancarExcecaoQuandoMotoristaNaoEncontrado() {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdMotorista(99L);
        dto.setIdWallbox(1L);

        when(motoristaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reservaService.agendar(dto));
        assertTrue(ex.getMessage().contains("Motorista não encontrado"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("CT08 - Deve lançar exceção ao agendar reserva com Wallbox inexistente (RF07)")
    void deveLancarExcecaoQuandoWallboxNaoEncontrada() {
        Motorista motorista = new Motorista();
        motorista.setId(1L);

        ReservaDTO dto = new ReservaDTO();
        dto.setIdMotorista(1L);
        dto.setIdWallbox(88L);

        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motorista));
        when(wallboxRepository.findById(88L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reservaService.agendar(dto));
        assertTrue(ex.getMessage().contains("Wallbox não encontrada"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("CT09 - Deve atualizar status da reserva para CANCELADA ou NO_SHOW (RF09)")
    void deveAtualizarStatusDaReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(5L);
        reserva.setStatusReserva(Reserva.StatusReserva.PENDENTE);

        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva atualizada = reservaService.atualizarStatus(5L, Reserva.StatusReserva.CANCELADA);

        assertEquals(Reserva.StatusReserva.CANCELADA, atualizada.getStatusReserva());
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    @DisplayName("CT10 - Deve listar reservas filtradas por Host e por Motorista")
    void deveListarReservasPorHostEMotorista() {
        Reserva r1 = new Reserva();
        r1.setId(1L);

        when(reservaRepository.findByWallboxHostId(10L)).thenReturn(List.of(r1));
        when(reservaRepository.findByMotoristaId(20L)).thenReturn(List.of(r1));

        assertEquals(1, reservaService.listarPorHost(10L).size());
        assertEquals(1, reservaService.listarPorMotorista(20L).size());
    }
}
