package com.example.fitcontrol.service;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(
            String nombre,
            String email,
            String password) {

        // Validar datos
        if (nombre == null || nombre.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                "Datos inválidos. La contraseña debe tener al menos 8 caracteres."
            );
        }

        // Normalizar correo
        email = email.trim().toLowerCase();

        // Comprobar si existe
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Este correo ya está registrado."
            );
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(nombre.trim());
        usuario.setEmail(email);

        // Cifrar contraseña
        usuario.setPassword(
            passwordEncoder.encode(password)
        );

        usuario.setProveedor("LOCAL");

        // Guardar en PostgreSQL
        return usuarioRepository.save(usuario);
    }
}
