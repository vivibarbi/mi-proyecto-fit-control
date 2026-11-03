
package com.example.fitcontrol.repository;

import com.example.fitcontrol.model.RegistroPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroPesoRepository
        extends JpaRepository<RegistroPeso, Long> {

    List<RegistroPeso> findByUsuarioId(Long usuarioId);
}
