package com.example.fitcontrol.config;

import com.example.fitcontrol.model.Usuario;
import com.example.fitcontrol.repository.UsuarioRepository;
import com.example.fitcontrol.service.GoogleOidcUserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return email -> {
            Usuario usuario = usuarioRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                    new UsernameNotFoundException("Usuario no encontrado")
                );

            // Las cuentas creadas exclusivamente con Google
            // no tienen contraseña local.
            String password = usuario.getPassword();

            if (password == null || password.isBlank()) {
                password = passwordEncoder.encode(UUID.randomUUID().toString());
            }

            return User.withUsername(usuario.getEmail())
                .password(password)
                .roles("USER")
                .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            GoogleOidcUserService googleOidcUserService)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index",
                    "/index.html",
                    "/api/fitness/**",
                    "/api/auth/registro",
                    "/api/auth/csrf",
                    "/api/auth/me",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                .requestMatchers("/api/perfil/**").authenticated()
                .requestMatchers("/dashboard.html").authenticated()

                .anyRequest().authenticated()
            )
            
            .exceptionHandling(ex -> ex
            .defaultAuthenticationEntryPointFor(
                (request, response, exception) -> {
                    response.sendError(401, "Debes iniciar sesión");
                },
                request -> request.getRequestURI().startsWith("/api/")
                )
            )

            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .loginPage("/")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"mensaje\":\"Inicio de sesión correcto\"}"
                    );
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Correo o contraseña incorrectos\"}"
                    );
                })
                .permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.oidcUserService(googleOidcUserService)
                )
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("/dashboard.html");
                })
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                })
            );

        return http.build();
    }
}
