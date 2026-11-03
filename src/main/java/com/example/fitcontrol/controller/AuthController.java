package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
public ResponseEntity<?> usuarioActual(Authentication auth) {

    if (auth == null
            || !auth.isAuthenticated()
            || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "No has iniciado sesión"));
    }

    return ResponseEntity.ok(
        Map.of("email", auth.getName())
    );
}
}
