package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.model.RegistroPeso;
import com.example.fitcontrol.repository.UsuarioRepository;
import com.example.fitcontrol.repository.RegistroPesoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/progreso")
public class RegistroPesoController {

    private final RegistroPesoRepository registroRepository;
    private final UsuarioRepository usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(RegistroPesoController.class);

    public RegistroPesoController(
            RegistroPesoRepository registroRepository,
            UsuarioRepository usuarioRepository) {

        this.registroRepository = registroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener usuario autenticado
    private Usuario obtenerUsuario(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        String email;

        if (principal instanceof OidcUser) {

            email = ((OidcUser) principal).getEmail();

        } else if (principal instanceof OAuth2User) {

            email = ((OAuth2User) principal).getAttribute("email");

        } else if (principal instanceof UserDetails) {

            email = ((UserDetails) principal).getUsername();

        } else {

            email = authentication.getName();
        }

        if (email == null || email.isBlank()) {
            return null;
        }

        return usuarioRepository.findByEmail(
            email.trim().toLowerCase()
        ).orElse(null);
    }

    // CONSULTAR HISTORIAL


    @GetMapping
    public ResponseEntity<?> obtenerRegistros(
        Authentication authentication) {

    try {

        logger.info("Consultando historial de peso");

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            logger.warn("No se encontró usuario autenticado");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión no válida"));
        }

        logger.info("Usuario encontrado ID: {}", usuario.getId());

        List<RegistroPeso> registros =
            registroRepository.findByUsuario_IdOrderByFechaAsc(
                usuario.getId()
            );

        logger.info("Registros encontrados: {}", registros.size());

        List<Map<String, Object>> resultado = registros.stream()
            .map(registro -> Map.<String, Object>of(
                "id", registro.getId(),
                "peso", registro.getPeso(),
                "fecha", registro.getFecha().toString()
            ))
            .toList();

        return ResponseEntity.ok(resultado);

    } catch (Exception e) {

        logger.error("ERROR AL CONSULTAR HISTORIAL DE PESO", e);

        throw e;
    }
}
    /*@GetMapping
    public ResponseEntity<?> obtenerRegistros(
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión no válida"));
        }

        List<RegistroPeso> registros =
            registroRepository.findByUsuario_IdOrderByFechaAsc(
                usuario.getId()
            );

        List<Map<String, Object>> resultado = registros.stream()
            .map(registro -> Map.<String, Object>of(
                "id", registro.getId(),
                "peso", registro.getPeso(),
                "fecha", registro.getFecha().toString()
            ))
            .toList();

        return ResponseEntity.ok(resultado);
    }
*/
    // REGISTRAR PESO
    @PostMapping
    public ResponseEntity<?> registrarPeso(
            @RequestBody Map<String, Object> datos,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión no válida"));
        }

        Object valor = datos.get("peso");

        if (!(valor instanceof Number)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Introduce un peso válido"));
        }

        double peso = ((Number) valor).doubleValue();

        if (!Double.isFinite(peso) || peso <= 0 || peso > 1000) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "El peso debe ser válido"));
        }

        LocalDate hoy = LocalDate.now();

        if (registroRepository.existsByUsuario_IdAndFecha(usuario.getId(), hoy)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "error",
                    "Ya registraste tu peso el día de hoy"
                ));
        }

        RegistroPeso registro = new RegistroPeso(peso, usuario, hoy);

        try {

            RegistroPeso guardado =
                registroRepository.saveAndFlush(registro);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensaje", "Peso registrado correctamente",
                    "id", guardado.getId(),
                    "peso", guardado.getPeso(),
                    "fecha", guardado.getFecha().toString()
                ));

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "error",
                    "Ya existe un registro para el día de hoy"
                ));
        }
    }
}
