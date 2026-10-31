package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.RegistroPeso;
import com.example.fitcontrol.repository.RegistroPesoRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/peso")
public class PesoController {

    private final RegistroPesoRepository repository;

    public PesoController(RegistroPesoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RegistroPeso> listar() {
        return repository.findAll();
    }

    @PostMapping
    public RegistroPeso guardar(@RequestBody Map<String, Double> datos) {

        Double peso = datos.get("peso");

        if (peso == null || !Double.isFinite(peso)
                || peso <= 0 || peso > 500) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Peso inválido"
            );
        }

        return repository.save(new RegistroPeso(peso));
    }
}