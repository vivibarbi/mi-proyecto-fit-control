
package com.example.fitcontrol.repository;

import com.example.fitcontrol.model.RegistroPeso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPesoRepository
        extends JpaRepository<RegistroPeso, Long> {

    List<RegistroPeso> findByUsuario_IdOrderByFechaAsc(Long usuarioId);

    Optional<RegistroPeso> findByUsuario_IdAndFecha(
        Long usuarioId,
        LocalDate fecha
    );

    boolean existsByUsuario_IdAndFecha(
        Long usuarioId,
        LocalDate fecha
    );
}
