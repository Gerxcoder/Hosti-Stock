package com.hostistock.dto;

public record DisponibilidadResponse(
    Long platoId,
    String nombrePlato,
    int unidadesDisponibles
) {}