package com.example.fitcontrol.service;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.UsuarioRepository;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    public GoogleOAuth2UserService(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User googleUser = super.loadUser(userRequest);

        String email = googleUser.getAttribute("email");
        String nombre = googleUser.getAttribute("name");
        Boolean emailVerificado =
            googleUser.getAttribute("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerificado)) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_no_verificado"),
                "Google no proporcionó un correo verificado."
            );
        }

        email = email.trim().toLowerCase();

        final String correo = email;
        final String nombreGoogle =
            (nombre == null || nombre.isBlank()) ? correo : nombre;

        Usuario usuario = usuarioRepository.findByEmail(correo)
            .orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setEmail(correo);
                nuevo.setNombre(nombreGoogle);
                nuevo.setProveedor("GOOGLE");
                return nuevo;
            });

// Si ya existía una cuenta local, conservamos su contraseña y permitimos también el acceso con Google.
        if ("LOCAL".equals(usuario.getProveedor())) {
            usuario.setProveedor("LOCAL_GOOGLE");
        }

        if (usuario.getNombre() == null ||
                usuario.getNombre().isBlank()) {
            usuario.setNombre(nombreGoogle);
        }

        usuarioRepository.save(usuario);

        return googleUser;
    }
}
