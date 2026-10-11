package com.recargafacil.repository;

import com.recargafacil.model.Motorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotoristaRepository extends JpaRepository<Motorista, Long> {
    Optional<Motorista> findByUsuarioEmail(String email);
    Optional<Motorista> findByEmail(String email);
    Optional<Motorista> findByUsuarioId(Long usuarioId);
}