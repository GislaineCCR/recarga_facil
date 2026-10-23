package com.recargafacil.service;

import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.model.Pagamento;
import com.recargafacil.model.Reserva;
import com.recargafacil.repository.PagamentoRepository;
import com.recargafacil.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PagamentoService (RF08 e RF09)")
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("CT01 - Deve calcular automaticamente a taxa de 25% do sistema e concluir a reserva (RF08/RF09)")
    void deveCalcularTaxaAutomaticaEConcluirReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setStatusReserva(Reserva.StatusReserva.PENDENTE);

        PagamentoDTO dto = new PagamentoDTO();
        dto.setIdReserva(1L);
        dto.setValorTotal(new BigDecimal("100.00"));
        dto.setTaxaSistema(null); // Deve calcular 25% automaticamente

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento resultado = pagamentoService.processar(dto);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("100.00"), resultado.getValorTotal());
        assertEquals(new BigDecimal("25.00"), resultado.getTaxaSistema());
        assertEquals(Pagamento.StatusPagamento.APROVADO, resultado.getStatusPagamento());
        assertEquals(Reserva.StatusReserva.CONCLUIDA, reserva.getStatusReserva());

        verify(reservaRepository, times(1)).save(reserva);
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("CT02 - Deve preservar a taxa do sistema quando informada explicitamente no DTO (RF08)")
    void devePreservarTaxaInformadaExplicitamente() {
        Reserva reserva = new Reserva();
        reserva.setId(2L);
        reserva.setStatusReserva(Reserva.StatusReserva.PENDENTE);

        PagamentoDTO dto = new PagamentoDTO();
        dto.setIdReserva(2L);
        dto.setValorTotal(new BigDecimal("60.00"));
        dto.setTaxaSistema(new BigDecimal("15.00"));

        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento resultado = pagamentoService.processar(dto);

        assertEquals(new BigDecimal("15.00"), resultado.getTaxaSistema());
        assertEquals(Pagamento.StatusPagamento.APROVADO, resultado.getStatusPagamento());
    }

    @Test
    @DisplayName("CT03 - Deve lançar exceção ao processar pagamento para reserva inexistente (RF08)")
    void deveLancarExcecaoQuandoReservaNaoExistir() {
        PagamentoDTO dto = new PagamentoDTO();
        dto.setIdReserva(999L);
        dto.setValorTotal(new BigDecimal("50.00"));

        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> pagamentoService.processar(dto));
        assertTrue(exception.getMessage().contains("Reserva não encontrada"));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("CT04 - Deve atualizar o status de um pagamento existente com sucesso (RF09)")
    void deveAtualizarStatusDoPagamento() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(10L);
        pagamento.setStatusPagamento(Pagamento.StatusPagamento.PENDENTE);

        when(pagamentoRepository.findById(10L)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento atualizado = pagamentoService.atualizarStatus(10L, Pagamento.StatusPagamento.REJEITADO);

        assertEquals(Pagamento.StatusPagamento.REJEITADO, atualizado.getStatusPagamento());
        verify(pagamentoRepository, times(1)).save(pagamento);
    }

    @Test
    @DisplayName("CT05 - Deve lançar exceção ao tentar atualizar pagamento inexistente (RF09)")
    void deveLancarExcecaoAoAtualizarPagamentoInexistente() {
        when(pagamentoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> pagamentoService.atualizarStatus(404L, Pagamento.StatusPagamento.APROVADO));
    }
}
