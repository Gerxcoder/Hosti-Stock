package com.hostistock.dto;

import com.hostistock.model.Prediccion;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PrediccionResponse(
    Long ingredienteId,
    String nombreIngrediente,
    LocalDate fechaPrediccion,
    BigDecimal consumoPrevisto,
    BigDecimal stockEstimado,
    BigDecimal recomendacionCompra
) {
    public static PrediccionResponse desdeEntidad(Prediccion p) {
        return new PrediccionResponse(
            p.getIngrediente().getId(),
            p.getIngrediente().getNombre(),
            p.getFechaPrediccion(),
            p.getConsumoPrevisto(),
            p.getStockEstimado(),
            p.getRecomendacionCompra()
        );
    }
}