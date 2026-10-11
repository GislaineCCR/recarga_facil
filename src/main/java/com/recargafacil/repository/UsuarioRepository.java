package com.recargafacil.repository;

import com.recargafacil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndSenha(String email, String senha);
    Optional<Usuario> findFirstByEmail(String email);
    Optional<Usuario> findFirstByEmailAndSenha(String email, String senha);
}