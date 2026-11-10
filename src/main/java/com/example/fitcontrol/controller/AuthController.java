package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.UsuarioRepository;
import com.example.fitcontrol.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @RequestBody Map<String, String> datos) {

        try {
            Usuario usuario = usuarioService.registrarUsuario(
                datos.get("nombre"),
                datos.get("email"),
                datos.get("password")
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensaje", "Usuario registrado correctamente",
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail()
                ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName()
        );
    }

   
@GetMapping("/me")
public ResponseEntity<?> obtenerUsuario(Authentication authentication) {

    if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión no autenticada"));
    }

    String email = authentication.getName().trim().toLowerCase();

    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElse(null);

    if (usuario == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", "Usuario autenticado, pero no encontrado en la base de datos",
                    "email", email
                ));
    }

    String nombre = usuario.getNombre();

    if (nombre == null || nombre.isBlank()) {
        nombre = usuario.getEmail();
    }

    return ResponseEntity.ok(Map.of(
            "nombre", nombre,
            "email", usuario.getEmail()
    ));
    }
 
}

