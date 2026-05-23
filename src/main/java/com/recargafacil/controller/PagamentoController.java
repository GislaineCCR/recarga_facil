package com.recargafacil.controller;

import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.model.Pagamento;
import com.recargafacil.model.Reserva;
import com.recargafacil.repository.PagamentoRepository;
import com.recargafacil.repository.ReservaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Payment Controller", description = "Endpoints for payment processing")
public class PagamentoController {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @PostMapping
    @Operation(summary = "Process a payment")
    public ResponseEntity<Pagamento> processarPagamento(@RequestBody PagamentoDTO dto) {
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Pagamento pagamento = new Pagamento();
        pagamento.setReserva(reserva);
        pagamento.setValorTotal(dto.getValorTotal());
        pagamento.setTaxaSistema(dto.getTaxaSistema());
        pagamento.setStatusPagamento(Pagamento.StatusPagamento.APROVADO);

        reserva.setStatusReserva(Reserva.StatusReserva.CONCLUIDA);
        reservaRepository.save(reserva);

        return ResponseEntity.ok(pagamentoRepository.save(pagamento));
    }

    @GetMapping
    @Operation(summary = "List all payments")
    public ResponseEntity<List<Pagamento>> listar() {
        return ResponseEntity.ok(pagamentoRepository.findAll());
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Find payment by reservation ID")
    public ResponseEntity<Pagamento> buscarPorReserva(@PathVariable Long reservaId) {
        return pagamentoRepository.findByReservaId(reservaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
