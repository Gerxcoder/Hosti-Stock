package com.hostistock.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PlatoRequest(
    @NotBlank(message = "El nombre del plato es obligatorio")
    String nombre,

    String categoria,

    @Valid
    List<RecetaItemRequest> receta
) {}
