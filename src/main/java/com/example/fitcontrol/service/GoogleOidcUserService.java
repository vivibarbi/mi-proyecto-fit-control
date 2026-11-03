package com.example.fitcontrol.service;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.UsuarioRepository;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;

    public GoogleOidcUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser googleUser = super.loadUser(userRequest);

        String email = googleUser.getEmail();
        String nombre = googleUser.getFullName();

        if (email == null || !Boolean.TRUE.equals(
                googleUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                "Google no proporcionó un correo verificado"
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
