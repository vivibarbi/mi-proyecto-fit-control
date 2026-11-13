package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.RegistroPeso;
import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.RegistroPesoRepository;
import com.example.fitcontrol.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
/*
@RestController
@RequestMapping("/api/peso")
public class PesoController {

    private final RegistroPesoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public PesoController(
            RegistroPesoRepository repository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<RegistroPeso> listar(Authentication auth) {

        Usuario usuario = obtenerUsuario(auth);

        return repository.findByUsuarioId(usuario.getId());
    }

    @PostMapping
    public RegistroPeso guardar(
            @RequestBody Map<String, Double> datos,
            Authentication auth) {

        Usuario usuario = obtenerUsuario(auth);

        Double peso = datos.get("peso");

        if (peso == null || !Double.isFinite(peso)
                || peso <= 0 || peso > 500) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Peso inválido"
            );
        }

        return repository.save(
            new RegistroPeso(peso, usuario)
        );
    }

    private Usuario obtenerUsuario(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesión"
            );
        }

        return usuarioRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no encontrado"
            ));
    }
}
 */