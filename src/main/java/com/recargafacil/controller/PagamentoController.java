package com.recargafacil.controller;

import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.dto.StatusUpdateDTO;
import com.recargafacil.model.Pagamento;
import com.recargafacil.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Payment Controller", description = "Endpoints para processamento financeiro e taxa da plataforma")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @PostMapping
    @Operation(summary = "RF08: Processar o pagamento de uma reserva com taxa de intermediação de 25%")
    public ResponseEntity<Pagamento> processarPagamento(@RequestBody PagamentoDTO dto) {
        return ResponseEntity.ok(pagamentoService.processar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os pagamentos registrados")
    public ResponseEntity<List<Pagamento>> listar() {
        return ResponseEntity.ok(pagamentoService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento pelo ID")
    public ResponseEntity<Pagamento> buscarPorId(@PathVariable Long id) {
        return pagamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "RF09: Atualizar status do pagamento (APROVADO, PENDENTE, REJEITADO)")
    public ResponseEntity<Pagamento> atualizarStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        Pagamento.StatusPagamento status = Pagamento.StatusPagamento.valueOf(dto.getStatus().toUpperCase());
        return ResponseEntity.ok(pagamentoService.atualizarStatus(id, status));
    }
}
