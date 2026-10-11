package com.recargafacil.service;

import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.model.Pagamento;
import com.recargafacil.model.Reserva;
import com.recargafacil.repository.PagamentoRepository;
import com.recargafacil.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * RF08: Gravar Pagamento
     * Inserir a transação na tabela Pagamento, registrando o valor total e a taxa do sistema (25%)
     * atrelados a uma reserva.
     */
    public Pagamento processar(PagamentoDTO dto) {
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada com ID: " + dto.getIdReserva()));

        BigDecimal valorTotal = dto.getValorTotal();
        BigDecimal taxaSistema = dto.getTaxaSistema();

        // Se a taxa de 25% não for enviada, calcula automaticamente no backend
        if (taxaSistema == null && valorTotal != null) {
            taxaSistema = valorTotal.multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setReserva(reserva);
        pagamento.setValorTotal(valorTotal);
        pagamento.setTaxaSistema(taxaSistema);
        pagamento.setStatusPagamento(Pagamento.StatusPagamento.APROVADO);

        // RF09: Atualiza status da reserva para CONCLUIDA após confirmação de pagamento
        reserva.setStatusReserva(Reserva.StatusReserva.CONCLUIDA);
        reservaRepository.save(reserva);

        return pagamentoRepository.save(pagamento);
    }

    /**
     * RF09: Atualizar Status do Pagamento
     */
    public Pagamento atualizarStatus(Long id, Pagamento.StatusPagamento novoStatus) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com ID: " + id));

        pagamento.setStatusPagamento(novoStatus);
        return pagamentoRepository.save(pagamento);
    }

    public List<Pagamento> listar() {
        return pagamentoRepository.findAll();
    }

    public Optional<Pagamento> buscarPorId(Long id) {
        return pagamentoRepository.findById(id);
    }
}
