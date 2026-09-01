package com.recargafacil.controller;

import com.recargafacil.dto.ReservaDTO;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Reserva;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.MotoristaRepository;
import com.recargafacil.repository.ReservaRepository;
import com.recargafacil.repository.WallboxRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * CONTROLLER REST: ReservaController
 * ============================================================================
 * FUNÇÃO NO SISTEMA:
 * Gerencia a criação e consulta de reservas de recarga solicitadas pelos motoristas.
 * 
 * CONEXÃO COM O FRONT-END:
 * - Tela 3 (Agendamento de Horário):
 *   * POST /api/reservas -> O front-end envia o payload com a data, horário, motorista
 *     e wallbox selecionada. O back-end cria a reserva com status 'PENDENTE'
 *     e devolve o ID da reserva para o fluxo de pagamento.
 * - Tela 4 (Checkout):
 *   * Utiliza o ID da reserva criada para processar o pagamento com a taxa de 25%.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*") // Permite integração direta com o Front-end Web sem bloqueio de CORS
@Tag(name = "Reservation Controller", description = "Endpoints para gerenciamento de agendamentos de recarga")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Autowired
    private WallboxRepository wallboxRepository;

    /**
     * CRIAÇÃO DE UMA NOVA RESERVA
     * CONEXÃO FRONT-END: Disparado pelo botão "Ir para Pagamento" na Tela 3 (Agendamento).
     */
    @PostMapping
    @Operation(summary = "Agendar uma nova reserva de recarga")
    public ResponseEntity<Reserva> agendar(@RequestBody ReservaDTO dto) {
        Long idMotorista = (dto.getIdMotorista() != null) ? dto.getIdMotorista() : 1L;
        Motorista motorista = motoristaRepository.findById(idMotorista)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com ID: " + idMotorista));

        Wallbox wallbox = wallboxRepository.findById(dto.getIdWallbox())
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada com ID: " + dto.getIdWallbox()));

        Reserva reserva = new Reserva();
        reserva.setMotorista(motorista);
        reserva.setWallbox(wallbox);
        reserva.setDataReserva(dto.getDataReserva());
        reserva.setHorario(dto.getHorario());
        reserva.setStatusReserva(Reserva.StatusReserva.PENDENTE);

        return ResponseEntity.ok(reservaRepository.save(reserva));
    }

    /**
     * LISTAGEM DE TODAS AS RESERVAS
     * CONEXÃO FRONT-END: Usado na aba "Recargas" (Minhas Reservas) do Bottom Navigation.
     */
    @GetMapping
    @Operation(summary = "Listar todas as reservas")
    public ResponseEntity<List<Reserva>> listar() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }

    /**
     * LISTAGEM DE RESERVAS POR WALLBOX
     * CONEXÃO FRONT-END: Usado para desabilitar horários já ocupados na grade da Tela 3.
     */
    @GetMapping("/wallbox/{wallboxId}")
    @Operation(summary = "Listar reservas de uma determinada Wallbox")
    public ResponseEntity<List<Reserva>> listarPorWallbox(@PathVariable Long wallboxId) {
        return ResponseEntity.ok(reservaRepository.findByWallboxId(wallboxId));
    }

    /**
     * CONSULTA DE RESERVA ESPECÍFICA POR ID
     * CONEXÃO FRONT-END: Usado para carregar o resumo da reserva na Tela 4 (Checkout).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva pelo ID")
    public ResponseEntity<Reserva> buscarPorId(@PathVariable Long id) {
        return reservaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
