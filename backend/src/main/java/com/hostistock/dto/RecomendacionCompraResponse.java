package com.hostistock.dto;

import com.hostistock.model.TipoUnidad;
import java.math.BigDecimal;

public record RecomendacionCompraResponse(
    Long ingredienteId,
    String nombreIngrediente,
    TipoUnidad unidad,
    BigDecimal stockActual,
    BigDecimal consumoPrevisto7Dias,
    BigDecimal stockEstimadoTrasPeriodo,
    BigDecimal cantidadRecomendada,
    String alerta
) {}