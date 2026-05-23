package com.example.recargafacil.repository;

import com.example.recargafacil.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
}