package com.hostistock.dto;

import com.hostistock.model.Plato;
import java.time.LocalDateTime;
import java.util.List;

public record PlatoResponse(
    Long id,
    String nombre,
    String categoria,
    List<RecetaItemResponse> receta,
    Integer unidadesDisponibles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PlatoResponse desdeEntidad(Plato plato, Integer unidadesDisponibles) {
        List<RecetaItemResponse> recetaDto = plato.getReceta().stream()
            .map(RecetaItemResponse::desdeEntidad)
            .toList();

        return new PlatoResponse(
            plato.getId(),
            plato.getNombre(),
            plato.getCategoria(),
            recetaDto,
            unidadesDisponibles,
            plato.getCreatedAt(),
            plato.getUpdatedAt()
        );
    }
}