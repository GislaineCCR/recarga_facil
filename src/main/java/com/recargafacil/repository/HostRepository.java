package com.recargafacil.repository;

import com.recargafacil.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByUsuarioEmail(String email);
    Optional<Host> findByEmail(String email);
    Optional<Host> findByUsuarioId(Long usuarioId);
}