package com.example.recargafacil.repository;

import com.example.recargafacil.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByWallboxId(Long wallboxId);
}