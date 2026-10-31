package com.example.fitcontrol.repository;

import com.example.fitcontrol.model.RegistroPeso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroPesoRepository
        extends JpaRepository<RegistroPeso, Long> {
}