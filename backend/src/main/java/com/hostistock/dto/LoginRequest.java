package com.hostistock.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El email o nombre es obligatorio")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}