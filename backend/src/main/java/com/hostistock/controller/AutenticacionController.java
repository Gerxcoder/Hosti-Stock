package com.hostistock.controller;

import com.hostistock.dto.LoginRequest;
import com.hostistock.dto.LoginResponse;
import com.hostistock.dto.RegistroBarRequest;
import com.hostistock.service.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroBarRequest request) {
        LoginResponse response = autenticacionService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = autenticacionService.login(request);
        return ResponseEntity.ok(response);
    }
}