package com.recargafacil.repository;

import com.recargafacil.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
}