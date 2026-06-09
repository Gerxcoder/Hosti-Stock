package com.hostistock.config;

import com.hostistock.security.JwtFiltroAutenticacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

// JWT stateless, CSRF off, CORS permisivo en desarrollo.
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final JwtFiltroAutenticacion jwtFiltro;

    public SeguridadConfig(JwtFiltroAutenticacion jwtFiltro) {
        this.jwtFiltro = jwtFiltro;
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        http
            // CORS abierto en desarrollo — en producción habría que restringir origins
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                return config;
            }))

            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/ping", "/api/version", "/api/auth/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }
}