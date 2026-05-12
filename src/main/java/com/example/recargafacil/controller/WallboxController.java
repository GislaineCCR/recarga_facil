package com.example.recargafacil.controller;

import com.example.recargafacil.dto.WallboxDTO;
import com.example.recargafacil.model.Host;
import com.example.recargafacil.model.Local;
import com.example.recargafacil.model.Wallbox;
import com.example.recargafacil.repository.HostRepository;
import com.example.recargafacil.repository.WallboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallboxes")
public class WallboxController {

    @Autowired
    private WallboxRepository wallboxRepository;

    @Autowired
    private HostRepository hostRepository;

    @PostMapping
    public ResponseEntity<Wallbox> cadastrar(@RequestBody WallboxDTO dto) {
        Host host = hostRepository.findById(dto.getIdHost())
                .orElseThrow(() -> new RuntimeException("Host não encontrado"));

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

        return ResponseEntity.ok(wallboxRepository.save(wallbox));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Wallbox>> buscarProximos(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(wallboxRepository.findNearby(lat, lng, radius));
    }

    @GetMapping
    public ResponseEntity<List<Wallbox>> listar() {
        return ResponseEntity.ok(wallboxRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallbox> buscarPorId(@PathVariable Long id) {
        return wallboxRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}