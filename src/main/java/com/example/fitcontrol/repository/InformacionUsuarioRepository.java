package com.example.fitcontrol.repository;

import com.example.fitcontrol.model.InformacionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InformacionUsuarioRepository
        extends JpaRepository<InformacionUsuario, Long> {

    Optional<InformacionUsuario> findByUsuarioId(Long usuarioId);
}
