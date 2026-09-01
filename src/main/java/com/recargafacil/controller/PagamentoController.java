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

/**
 * ============================================================================
 * CONTROLLER REST: PagamentoController
 * ============================================================================
 * FUNÇÃO NO SISTEMA:
 * Processa a liquidação financeira das reservas, aplica a taxa de intermediação
 * de 25% da plataforma e, com o pagamento aprovado, autoriza a liberação
 * do endereço completo e das instruções de acesso ao imóvel.
 * 
 * CONEXÃO COM O FRONT-END:
 * - Tela 4 (Pagamento e Checkout):
 *   * POST /api/pagamentos -> Recebe o valor total e a taxa da plataforma (25%)
 *     calculados no front-end. Ao aprovar, o status da Reserva passa para 'CONCLUIDA'.
 * - Tela 5 (Sucesso e Desbloqueio):
 *   * A resposta deste endpoint confirma o sucesso e entrega os dados para a interface
 *     exibir o endereço completo, código de portão, interfone e botões Waze/WhatsApp.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin(origins = "*") // Permite chamadas diretas do Front-end Web (CORS liberado)
@Tag(name = "Payment Controller", description = "Endpoints para processamento financeiro e taxa da plataforma")
public class PagamentoController {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * PROCESSAMENTO DE PAGAMENTO (PIX OU CARTÃO)
     * CONEXÃO FRONT-END: Disparado pelo botão "Confirmar e Pagar R$ 37,50" na Tela 4 (Checkout).
     */
    @PostMapping
    @Operation(summary = "Processar o pagamento de uma reserva com taxa de intermediação de 25%")
    public ResponseEntity<Pagamento> processarPagamento(@RequestBody PagamentoDTO dto) {
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada com ID: " + dto.getIdReserva()));

        Pagamento pagamento = new Pagamento();
        pagamento.setReserva(reserva);
        pagamento.setValorTotal(dto.getValorTotal());
        pagamento.setTaxaSistema(dto.getTaxaSistema());
        
        // Define o status como APROVADO
        pagamento.setStatusPagamento(Pagamento.StatusPagamento.APROVADO);

        // Atualiza o status da Reserva para CONCLUIDA (desbloqueia endereço)
        reserva.setStatusReserva(Reserva.StatusReserva.CONCLUIDA);
        reservaRepository.save(reserva);

        return ResponseEntity.ok(pagamentoRepository.save(pagamento));
    }

    /**
     * LISTAGEM DE TODOS OS PAGAMENTOS
     */
    @GetMapping
    @Operation(summary = "Listar todos os pagamentos registrados")
    public ResponseEntity<List<Pagamento>> listar() {
        return ResponseEntity.ok(pagamentoRepository.findAll());
    }

    /**
     * CONSULTA DE PAGAMENTO POR RESERVA
     */
    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Buscar pagamento pelo ID da reserva")
    public ResponseEntity<Pagamento> buscarPorReserva(@PathVariable Long reservaId) {
        return pagamentoRepository.findByReservaId(reservaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
