package com.hostistock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroBarRequest(
    @NotBlank(message = "El nombre del bar es obligatorio")
    String nombre,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password
) {}