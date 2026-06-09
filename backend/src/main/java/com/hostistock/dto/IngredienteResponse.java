package com.hostistock.dto;

import com.hostistock.model.Ingrediente;
import com.hostistock.model.TipoUnidad;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredienteResponse(
    Long id,
    String nombre,
    TipoUnidad unidad,
    BigDecimal stockActual,
    BigDecimal stockMinimo,
    boolean stockBajo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static IngredienteResponse desdeEntidad(Ingrediente entidad) {
        return new IngredienteResponse(
            entidad.getId(),
            entidad.getNombre(),
            entidad.getUnidad(),
            entidad.getStockActual(),
            entidad.getStockMinimo(),
            entidad.getStockActual().compareTo(entidad.getStockMinimo()) < 0,
            entidad.getCreatedAt(),
            entidad.getUpdatedAt()
        );
    }
}