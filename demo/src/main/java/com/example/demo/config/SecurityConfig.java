package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
            List.of("http://localhost:4200")
        );

        config.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        config.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"status\":401,\"mensaje\":\"Token requerido o inválido\"}"
                        );
                    }
                )
                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"status\":403,\"mensaje\":\"No tienes permisos para esta operación\"}"
                        );
                    }
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Permitir solicitudes CORS preliminares.
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // Endpoints de autenticación.
                .requestMatchers(
                    "/api/auth/**"
                ).permitAll()

                // Exportar el reporte requiere autenticación.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/activos/reportes/zip"
                ).hasAnyRole("ADMIN", "USER")

                // Consultas públicas para cargar el dashboard.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/activos",
                    "/api/categorias"
                ).permitAll()

                // Otras consultas requieren autenticación.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/activos/**",
                    "/api/categorias/**"
                ).hasAnyRole("ADMIN", "USER")

                // Solo ADMIN puede crear activos o categorías.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/activos",
                    "/api/activos/**",
                    "/api/categorias",
                    "/api/categorias/**"
                ).hasRole("ADMIN")

                // Solo ADMIN puede actualizar activos o categorías.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/activos",
                    "/api/activos/**",
                    "/api/categorias",
                    "/api/categorias/**"
                ).hasRole("ADMIN")

                // Solo ADMIN puede realizar actualizaciones parciales.
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/activos",
                    "/api/activos/**",
                    "/api/categorias",
                    "/api/categorias/**"
                ).hasRole("ADMIN")

                // Solo ADMIN puede eliminar.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/activos",
                    "/api/activos/**",
                    "/api/categorias",
                    "/api/categorias/**"
                ).hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación.
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}