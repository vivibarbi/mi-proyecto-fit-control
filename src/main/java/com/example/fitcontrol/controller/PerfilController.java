package com.example.fitcontrol.controller;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.model.InformacionUsuario;
import com.example.fitcontrol.repository.UsuarioRepository;
import com.example.fitcontrol.repository.InformacionUsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final InformacionUsuarioRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilController(
            InformacionUsuarioRepository perfilRepository,
            UsuarioRepository usuarioRepository) {

        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener correo del usuario autenticado
    private String obtenerEmail(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser) {
            return ((OidcUser) principal).getEmail();
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return authentication.getName();
    }

    // CONSULTAR INFORMACIÓN DEL USUARIO
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> consultarPerfil(
            Authentication authentication) {

        String email = obtenerEmail(authentication);

        Usuario usuario = usuarioRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
        }

        return perfilRepository.findByUsuarioId(usuario.getId())
                .<ResponseEntity<?>>map(perfil ->
                    ResponseEntity.ok(new PerfilResponse(
                        perfil.getEdad(),
                        perfil.getPeso(),
                        perfil.getSexo(),
                        perfil.getAltura(),
                        perfil.getPais(),
                        perfil.getPreferencia()
                    ))
                )
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GUARDAR INFORMACIÓN POR PRIMERA VEZ
    @PostMapping
    @Transactional 
    public ResponseEntity<?> guardarPerfil(
            @RequestBody PerfilRequest datos,
            Authentication authentication) {

        String email = obtenerEmail(authentication);

        Usuario usuario = usuarioRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
        }

        // Evitar registros duplicados
        /*if (perfilRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Ya tienes un perfil registrado"));
        }*/

        // Validaciones básicas
        if (datos == null ||
            datos.edad() == null || datos.edad() < 13 ||
            datos.edad() > 120 ||
            datos.peso() == null || datos.peso().signum() <= 0 ||
            datos.altura() == null || datos.altura().signum() <= 0 ||
            datos.sexo() == null || datos.sexo().isBlank() ||
            datos.pais() == null || datos.pais().isBlank() ||
            datos.preferencia() == null || datos.preferencia().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Completa correctamente todos los campos"));
        }

        //InformacionUsuario perfil = new InformacionUsuario();
        InformacionUsuario perfil = perfilRepository
            .findByUsuarioId(usuario.getId())
            .orElseGet(InformacionUsuario::new);

        perfil.setUsuario(usuario);
        perfil.setEdad(datos.edad());
        perfil.setPeso(datos.peso());
        perfil.setSexo(datos.sexo());
        perfil.setAltura(datos.altura());
        perfil.setPais(datos.pais());
        perfil.setPreferencia(datos.preferencia());

        perfilRepository.save(perfil);

        return ResponseEntity.ok(
                Map.of("mensaje", "Perfil guardado correctamente")
        );
    }

    public record PerfilRequest(
            Integer edad,
            BigDecimal peso,
            String sexo,
            BigDecimal altura,
            String pais,
            String preferencia
    ) {}

    public record PerfilResponse(
            Integer edad,
            BigDecimal peso,
            String sexo,
            BigDecimal altura,
            String pais,
            String preferencia
    ) {}
}
