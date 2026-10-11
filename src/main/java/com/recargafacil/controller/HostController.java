package com.recargafacil.controller;

import com.recargafacil.dto.HostDTO;
import com.recargafacil.dto.WallboxDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Pagamento;
import com.recargafacil.model.Reserva;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.PagamentoRepository;
import com.recargafacil.service.HostService;
import com.recargafacil.service.ReservaService;
import com.recargafacil.service.WallboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/hosts")
@Tag(name = "Host Controller", description = "Endpoints para gerenciamento do Portal do Anfitrião (Host)")
public class HostController {

    @Autowired
    private HostService hostService;

    @Autowired
    private WallboxService wallboxService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @PostMapping
    @Operation(summary = "Cadastrar novo anfitrião com dados pessoais e bancários")
    public ResponseEntity<Host> cadastrar(@RequestBody HostDTO dto) {
        return ResponseEntity.ok(hostService.cadastrar(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os anfitriões")
    public ResponseEntity<List<Host>> listar() {
        return ResponseEntity.ok(hostService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar anfitrião por ID")
    public ResponseEntity<Host> buscarPorId(@PathVariable Long id) {
        return hostService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/wallboxes")
    @Operation(summary = "Listar as estações pertencentes a um anfitrião específico")
    public ResponseEntity<List<Wallbox>> listarWallboxesDoHost(@PathVariable Long id) {
        return ResponseEntity.ok(wallboxService.listarPorHost(id));
    }

    @PostMapping("/{id}/wallboxes")
    @Operation(summary = "Cadastrar nova estação para o anfitrião autenticado")
    public ResponseEntity<Wallbox> cadastrarWallboxParaHost(@PathVariable Long id, @RequestBody WallboxDTO dto) {
        dto.setIdHost(id);
        return ResponseEntity.ok(wallboxService.cadastrar(dto));
    }

    @GetMapping("/{id}/reservas")
    @Operation(summary = "Listar quem e quando agendou as estações deste anfitrião")
    public ResponseEntity<List<Map<String, Object>>> listarReservasDoHost(@PathVariable Long id) {
        List<Reserva> reservas = reservaService.listarPorHost(id);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Reserva r : reservas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idReserva", r.getId());
            item.put("dataReserva", r.getDataReserva());
            item.put("horario", r.getHorario());
            item.put("statusReserva", r.getStatusReserva());

            // Dados do Motorista que agendou
            if (r.getMotorista() != null) {
                item.put("motoristaId", r.getMotorista().getId());
                item.put("motoristaNome", r.getMotorista().getNome());
                item.put("motoristaEmail", r.getMotorista().getEmail());
                item.put("motoristaTelefone", r.getMotorista().getTelefone());
            }

            // Dados do Veículo
            if (r.getVeiculo() != null) {
                item.put("veiculoModelo", r.getVeiculo().getMarca() + " " + r.getVeiculo().getModelo());
                item.put("veiculoPlaca", r.getVeiculo().getPlaca());
                item.put("veiculoConector", r.getVeiculo().getTipoConector());
            } else {
                item.put("veiculoModelo", "Veículo Elétrico");
                item.put("veiculoPlaca", "N/A");
            }

            // Dados da Wallbox
            if (r.getWallbox() != null) {
                item.put("wallboxId", r.getWallbox().getId());
                item.put("wallboxModelo", r.getWallbox().getModelo());
                item.put("precoHora", r.getWallbox().getPrecoHora());
            }

            // Pagamento e Valores
            Optional<Pagamento> pagOpt = pagamentoRepository.findByReservaId(r.getId());
            if (pagOpt.isPresent()) {
                Pagamento pag = pagOpt.get();
                item.put("valorTotal", pag.getValorTotal());
                item.put("taxaSistema", pag.getTaxaSistema());
                BigDecimal valorLiquido = pag.getValorTotal().subtract(pag.getTaxaSistema());
                item.put("valorLiquidoHost", valorLiquido);
                item.put("statusPagamento", pag.getStatusPagamento());
            } else {
                // Cálculo estimado padrão caso não haja registro de pagamento (ex: 2h)
                BigDecimal preco = (r.getWallbox() != null && r.getWallbox().getPrecoHora() != null)
                        ? r.getWallbox().getPrecoHora() : BigDecimal.valueOf(15.00);
                BigDecimal subtotal = preco.multiply(BigDecimal.valueOf(2));
                BigDecimal taxa = subtotal.multiply(BigDecimal.valueOf(0.25));
                item.put("valorTotal", subtotal.add(taxa));
                item.put("taxaSistema", taxa);
                item.put("valorLiquidoHost", subtotal);
                item.put("statusPagamento", "PENDENTE");
            }

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}/financeiro")
    @Operation(summary = "Resumo financeiro dos recebíveis do anfitrião")
    public ResponseEntity<Map<String, Object>> resumoFinanceiroDoHost(@PathVariable Long id) {
        List<Reserva> reservas = reservaService.listarPorHost(id);

        BigDecimal totalFaturado = BigDecimal.ZERO;
        BigDecimal totalRecebidoLiquido = BigDecimal.ZERO;
        BigDecimal taxaPlataformaTotal = BigDecimal.ZERO;

        for (Reserva r : reservas) {
            Optional<Pagamento> pagOpt = pagamentoRepository.findByReservaId(r.getId());
            if (pagOpt.isPresent()) {
                Pagamento p = pagOpt.get();
                totalFaturado = totalFaturado.add(p.getValorTotal());
                taxaPlataformaTotal = taxaPlataformaTotal.add(p.getTaxaSistema());
                totalRecebidoLiquido = totalRecebidoLiquido.add(p.getValorTotal().subtract(p.getTaxaSistema()));
            } else {
                BigDecimal preco = (r.getWallbox() != null && r.getWallbox().getPrecoHora() != null)
                        ? r.getWallbox().getPrecoHora() : BigDecimal.valueOf(15.00);
                BigDecimal subtotal = preco.multiply(BigDecimal.valueOf(2));
                BigDecimal taxa = subtotal.multiply(BigDecimal.valueOf(0.25));
                totalFaturado = totalFaturado.add(subtotal.add(taxa));
                taxaPlataformaTotal = taxaPlataformaTotal.add(taxa);
                totalRecebidoLiquido = totalRecebidoLiquido.add(subtotal);
            }
        }

        List<Wallbox> wallboxes = wallboxService.listarPorHost(id);

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("hostId", id);
        resumo.put("totalWallboxes", wallboxes.size());
        resumo.put("totalSessoes", reservas.size());
        resumo.put("totalFaturado", totalFaturado.setScale(2, RoundingMode.HALF_UP));
        resumo.put("taxaPlataforma", taxaPlataformaTotal.setScale(2, RoundingMode.HALF_UP));
        resumo.put("totalRecebidoLiquido", totalRecebidoLiquido.setScale(2, RoundingMode.HALF_UP));

        return ResponseEntity.ok(resumo);
    }
}