package com.hostistock.dto;

import com.hostistock.model.MovimientoStock;
import com.hostistock.model.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoStockResponse(
    Long id,
    Long ingredienteId,
    String nombreIngrediente,
    TipoMovimiento tipo,
    BigDecimal cantidad,
    String descripcion,
    LocalDateTime createdAt
) {
    public static MovimientoStockResponse desdeEntidad(MovimientoStock m) {
        return new MovimientoStockResponse(
            m.getId(),
            m.getIngrediente().getId(),
            m.getIngrediente().getNombre(),
            m.getTipo(),
            m.getCantidad(),
            m.getDescripcion(),
            m.getCreatedAt()
        );
    }
}
