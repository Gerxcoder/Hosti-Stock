package com.hostistock.dto;

import com.hostistock.model.PlatoIngrediente;
import com.hostistock.model.TipoUnidad;
import java.math.BigDecimal;

public record RecetaItemResponse(
    Long ingredienteId,
    String nombreIngrediente,
    TipoUnidad unidad,
    BigDecimal cantidad
) {
    public static RecetaItemResponse desdeEntidad(PlatoIngrediente pi) {
        return new RecetaItemResponse(
            pi.getIngrediente().getId(),
            pi.getIngrediente().getNombre(),
            pi.getIngrediente().getUnidad(),
            pi.getCantidad()
        );
    }
}