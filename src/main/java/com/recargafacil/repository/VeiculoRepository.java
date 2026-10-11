package com.recargafacil.repository;

import com.recargafacil.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findByMotoristaId(Long motoristaId);
}
