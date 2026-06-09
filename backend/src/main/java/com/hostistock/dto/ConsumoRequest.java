package com.hostistock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConsumoRequest(
    @NotNull(message = "El ID del plato es obligatorio")
    Long platoId,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    Integer cantidad
) {}