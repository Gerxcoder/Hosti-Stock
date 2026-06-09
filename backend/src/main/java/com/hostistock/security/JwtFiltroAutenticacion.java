package com.hostistock.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltroAutenticacion extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFiltroAutenticacion(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.validarToken(token)) {
                Long barId = jwtUtil.obtenerBarId(token);
                // Usamos barId como principal — sin roles, todos los bares tienen el mismo nivel
                UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(barId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        chain.doFilter(request, response);
    }
}