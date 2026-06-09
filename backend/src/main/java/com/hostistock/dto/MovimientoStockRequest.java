package com.hostistock.dto;

import com.hostistock.model.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MovimientoStockRequest(
    @NotNull(message = "El ID del ingrediente es obligatorio")
    Long ingredienteId,

    @NotNull(message = "El tipo de movimiento es obligatorio")
    TipoMovimiento tipo,

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que cero")
    BigDecimal cantidad,

    String descripcion
) {}