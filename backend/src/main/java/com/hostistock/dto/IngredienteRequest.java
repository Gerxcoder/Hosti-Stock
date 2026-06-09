package com.hostistock.dto;

import com.hostistock.model.TipoUnidad;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record IngredienteRequest(
    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    String nombre,

    @NotNull(message = "La unidad de medida es obligatoria")
    TipoUnidad unidad,

    @NotNull(message = "El stock actual es obligatorio")
    @DecimalMin(value = "0", message = "El stock actual no puede ser negativo")
    BigDecimal stockActual,

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0", message = "El stock mínimo no puede ser negativo")
    BigDecimal stockMinimo
) {}