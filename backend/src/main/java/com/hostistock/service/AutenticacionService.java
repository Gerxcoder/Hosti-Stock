package com.hostistock.service;

import com.hostistock.dto.LoginRequest;
import com.hostistock.dto.LoginResponse;
import com.hostistock.dto.RegistroBarRequest;
import com.hostistock.model.Bar;
import com.hostistock.exception.CredencialesInvalidasException;
import com.hostistock.repository.BarRepository;
import com.hostistock.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {
    private final BarRepository barRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AutenticacionService(BarRepository barRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtUtil jwtUtil) {
        this.barRepository = barRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponse registrar(RegistroBarRequest request) {
        if (barRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Ya existe un bar registrado con el email: " + request.email());
        }

        Bar bar = new Bar();
        bar.setNombre(request.nombre());
        bar.setEmail(request.email());
        bar.setPasswordHash(passwordEncoder.encode(request.password()));

        bar = barRepository.save(bar);
        String token = jwtUtil.generarToken(bar.getId(), bar.getEmail());
        return new LoginResponse(token, bar.getId(), bar.getNombre());
    }

    // Mensaje de error genérico intencionado — no decir si falla email o contraseña.
    public LoginResponse login(LoginRequest request) {
        Bar bar = barRepository.findByEmail(request.email())
            .or(() -> barRepository.findByNombreIgnoreCase(request.email()))
            .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), bar.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(bar.getId(), bar.getEmail());
        return new LoginResponse(token, bar.getId(), bar.getNombre());
    }
}