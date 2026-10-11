package com.recargafacil.service;

import com.recargafacil.dto.AgendaDTO;
import com.recargafacil.model.Agenda;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.AgendaRepository;
import com.recargafacil.repository.WallboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AgendaService {

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private WallboxRepository wallboxRepository;

    /**
     * RF05: Gerenciar Disponibilidade
     * Inserir e atualizar os dias da semana e horários de funcionamento na tabela Agenda.
     */
    public Agenda cadastrar(AgendaDTO dto) {
        Wallbox wallbox = wallboxRepository.findById(dto.getIdWallbox())
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada com ID: " + dto.getIdWallbox()));

        Agenda agenda = new Agenda();
        agenda.setWallbox(wallbox);
        agenda.setDiaSemana(dto.getDiaSemana());
        agenda.setHorarioInicio(dto.getHorarioInicio());
        agenda.setHorarioFim(dto.getHorarioFim());
        agenda.setEstaAtivo(dto.getEstaAtivo() != null ? dto.getEstaAtivo() : true);

        return agendaRepository.save(agenda);
    }

    public List<Agenda> listar() {
        return agendaRepository.findAll();
    }

    public List<Agenda> listarPorWallbox(Long idWallbox) {
        return agendaRepository.findByWallboxId(idWallbox);
    }

    public Optional<Agenda> buscarPorId(Long id) {
        return agendaRepository.findById(id);
    }

    public void remover(Long id) {
        agendaRepository.deleteById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public List<Agenda> salvarGrade(Long idWallbox, List<AgendaDTO> grade) {
        Wallbox wallbox = wallboxRepository.findById(idWallbox)
                .orElseThrow(() -> new RuntimeException("Wallbox não encontrada com ID: " + idWallbox));

        List<Agenda> existentes = agendaRepository.findByWallboxId(idWallbox);

        for (AgendaDTO dto : grade) {
            Agenda agenda = existentes.stream()
                    .filter(a -> a.getDiaSemana() == dto.getDiaSemana())
                    .findFirst()
                    .orElse(new Agenda());

            agenda.setWallbox(wallbox);
            agenda.setDiaSemana(dto.getDiaSemana());
            agenda.setHorarioInicio(dto.getHorarioInicio());
            agenda.setHorarioFim(dto.getHorarioFim());
            agenda.setEstaAtivo(dto.getEstaAtivo() != null ? dto.getEstaAtivo() : false);

            agendaRepository.save(agenda);
        }

        return agendaRepository.findByWallboxId(idWallbox);
    }

    public Agenda alternarStatusDia(Long idAgenda) {
        Agenda agenda = agendaRepository.findById(idAgenda)
                .orElseThrow(() -> new RuntimeException("Agenda não encontrada com ID: " + idAgenda));
        agenda.setEstaAtivo(!Boolean.TRUE.equals(agenda.getEstaAtivo()));
        return agendaRepository.save(agenda);
    }
}
