package com.recargafacil.controller;

import com.recargafacil.dto.LoginRequest;
import com.recargafacil.model.Pagamento;
import com.recargafacil.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Controller", description = "Endpoints para autenticação e dados do Painel Administrativo")
public class AdminController {

    @Autowired
    private MotoristaService motoristaService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private HostService hostService;

    @Autowired
    private WallboxService wallboxService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private PagamentoService pagamentoService;

    @PostMapping("/login")
    @Operation(summary = "Autenticação do Administrador (admin / admin)")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Validação das credenciais administrativas solicitadas (admin / admin)
        String usuarioOuEmail = request.getEmail();
        String senha = request.getSenha();

        if (("admin".equalsIgnoreCase(usuarioOuEmail) || "admin@recargafacil.com".equalsIgnoreCase(usuarioOuEmail)) 
                && "admin".equals(senha)) {
            response.put("success", true);
            response.put("token", "admin-session-active-token");
            response.put("nome", "Administrador Geral");
            response.put("role", "ADMIN");
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Credenciais de administrador inválidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Métricas consolidadas para o Dashboard Administrativo")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalMotoristas", motoristaService.listar().size());
        stats.put("totalVeiculos", veiculoService.listar().size());
        stats.put("totalHosts", hostService.listar().size());
        stats.put("totalWallboxes", wallboxService.listar().size());
        stats.put("totalReservas", reservaService.listar().size());

        List<Pagamento> pagamentos = pagamentoService.listar();
        stats.put("totalPagamentos", pagamentos.size());

        BigDecimal faturamentoTotal = pagamentos.stream()
                .filter(p -> p.getStatusPagamento() == Pagamento.StatusPagamento.APROVADO)
                .map(Pagamento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxaSistemaTotal = pagamentos.stream()
                .filter(p -> p.getStatusPagamento() == Pagamento.StatusPagamento.APROVADO)
                .map(Pagamento::getTaxaSistema)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("faturamentoTotal", faturamentoTotal);
        stats.put("taxaSistemaTotal", taxaSistemaTotal);

        return ResponseEntity.ok(stats);
    }
}
