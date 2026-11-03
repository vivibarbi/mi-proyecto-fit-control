package com.example.fitcontrol.repository;

import com.example.fitcontrol.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

// Buscar usuario por correo electrónico
    Optional<Usuario> findByEmail(String email);

// Comprobar si un correo ya está registrado
    boolean existsByEmail(String email);
}
