package com.hostistock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecetaItemRequest(
    @NotNull(message = "El ID del ingrediente es obligatorio")
    Long ingredienteId,

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que cero")
    BigDecimal cantidad
) {}