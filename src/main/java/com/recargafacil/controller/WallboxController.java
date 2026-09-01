package com.recargafacil.controller;

import com.recargafacil.dto.WallboxDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Local;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.HostRepository;
import com.recargafacil.repository.WallboxRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * CONTROLLER REST: WallboxController
 * ============================================================================
 * FUNÇÃO NO SISTEMA:
 * Expõe as rotas REST para consulta de pontos de recarga no mapa e cadastro
 * de novas estações por anfitriões (Hosts).
 * 
 * CONEXÃO COM O FRONT-END:
 * - Tela 1 (Busca no Mapa):
 *   * GET /api/wallboxes -> Retorna todas as estações para renderizar os marcadores no Leaflet.
 *   * GET /api/wallboxes/nearby?lat=...&lng=...&radius=... -> Busca por raio de proximidade do GPS.
 * - Tela 2 (Detalhes da Wallbox):
 *   * GET /api/wallboxes/{id} -> Carrega dados completos da estação selecionada.
 * - Segurança:
 *   * Endereço completo permanece protegido até a confirmação do pagamento.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/wallboxes")
@CrossOrigin(origins = "*") // Permite chamadas diretas do Front-end Web (CORS liberado)
@Tag(name = "Wallbox Controller", description = "Endpoints para gerenciamento e busca de pontos de recarga")
public class WallboxController {

    @Autowired
    private WallboxRepository wallboxRepository;

    @Autowired
    private HostRepository hostRepository;

    /**
     * CADASTRO DE NOVA WALLBOX
     * CONEXÃO FRONT-END: Utilizado quando um anfitrião cadastra um novo ponto na plataforma.
     */
    @PostMapping
    @Operation(summary = "Cadastrar uma nova Wallbox no sistema")
    public ResponseEntity<Wallbox> cadastrar(@RequestBody WallboxDTO dto) {
        Host host = hostRepository.findById(dto.getIdHost())
                .orElseThrow(() -> new RuntimeException("Host não encontrado com ID: " + dto.getIdHost()));

        Local local = new Local();
        local.setLatitude(dto.getLatitude());
        local.setLongitude(dto.getLongitude());
        local.setEnderecoCompleto(dto.getEnderecoCompleto());
        local.setTipoAmbiente(dto.getTipoAmbiente());
        local.setInstrucoesAcesso(dto.getInstrucoesAcesso());

        Wallbox wallbox = new Wallbox();
        wallbox.setHost(host);
        wallbox.setLocal(local);
        wallbox.setModelo(dto.getModelo());
        wallbox.setPotencia(dto.getPotencia());
        if (dto.getPrecoHora() != null) {
            wallbox.setPrecoHora(dto.getPrecoHora());
        }

        return ResponseEntity.ok(wallboxRepository.save(wallbox));
    }

    /**
     * BUSCA DE PONTOS PRÓXIMOS POR GEOLOCALIZAÇÃO (HAVERSINE)
     * CONEXÃO FRONT-END: Tela 1 (Busca no Mapa).
     */
    @GetMapping("/nearby")
    @Operation(summary = "Buscar pontos de recarga próximos via coordenadas GPS e raio em KM")
    public ResponseEntity<List<Wallbox>> buscarProximos(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "15.0") Double radius) {
        List<Wallbox> proximos = wallboxRepository.findNearby(lat, lng, radius);
        return ResponseEntity.ok(proximos);
    }

    /**
     * LISTAGEM GERAL DE TODAS AS WALLBOXES DISPONÍVEIS
     * CONEXÃO FRONT-END: Tela 1 (Busca no Mapa).
     */
    @GetMapping
    @Operation(summary = "Listar todas as Wallboxes cadastradas")
    public ResponseEntity<List<Wallbox>> listar() {
        return ResponseEntity.ok(wallboxRepository.findAll());
    }

    /**
     * CONSULTA DE DETALHES DE UMA WALLBOX POR ID
     * CONEXÃO FRONT-END: Tela 2 (Detalhes da Wallbox).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de uma Wallbox pelo ID")
    public ResponseEntity<Wallbox> buscarPorId(@PathVariable Long id) {
        return wallboxRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}