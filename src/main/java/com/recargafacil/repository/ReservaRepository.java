package com.recargafacil.repository;

import com.recargafacil.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByWallboxId(Long wallboxId);
}