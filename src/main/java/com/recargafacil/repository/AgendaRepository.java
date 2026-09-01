package com.recargafacil.repository;

import com.recargafacil.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByWallboxId(Long wallboxId);
}