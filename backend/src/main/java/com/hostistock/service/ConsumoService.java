package com.hostistock.service;

import com.hostistock.dto.ConsumoRequest;
import com.hostistock.dto.MovimientoStockResponse;
import com.hostistock.model.*;
import com.hostistock.exception.RecursoNoEncontradoException;
import com.hostistock.exception.StockInsuficienteException;
import com.hostistock.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumoService {

    private final PlatoRepository platoRepo;
    private final BarRepository barRepo;
    private final IngredienteRepository ingredienteRepo;
    private final MovimientoStockRepository movimientoRepo;

    public ConsumoService(PlatoRepository platoRepo,
                           BarRepository barRepo,
                           IngredienteRepository ingredienteRepo,
                           MovimientoStockRepository movimientoRepo) {
        this.platoRepo = platoRepo;
        this.barRepo = barRepo;
        this.ingredienteRepo = ingredienteRepo;
        this.movimientoRepo = movimientoRepo;
    }

    // Verifica stock de todos antes de descontar nada. @Transactional garantiza rollback total si falla.
    @Transactional
    public List<MovimientoStockResponse> registrarConsumo(Long barId, ConsumoRequest request) {
        Plato plato = platoRepo.findByIdAndBarId(request.platoId(), barId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el plato con ID " + request.platoId()));

        if (plato.getReceta() == null || plato.getReceta().isEmpty()) {
            throw new IllegalArgumentException(
                "El plato '" + plato.getNombre() + "' no tiene receta definida");
        }

        Bar bar = barRepo.findById(barId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bar no encontrado"));

        // Validar stock antes de tocar nada
        for (PlatoIngrediente pi : plato.getReceta()) {
            BigDecimal cantidadNecesaria = pi.getCantidad()
                .multiply(BigDecimal.valueOf(request.cantidad()));
            Ingrediente ingrediente = pi.getIngrediente();

            if (ingrediente.getStockActual().compareTo(cantidadNecesaria) < 0) {
                throw new StockInsuficienteException(
                    "Stock insuficiente de '" + ingrediente.getNombre() +
                    "': disponible=" + ingrediente.getStockActual() +
                    " " + ingrediente.getUnidad() +
                    ", necesario=" + cantidadNecesaria +
                    " " + ingrediente.getUnidad());
            }
        }

        List<MovimientoStockResponse> movimientos = new ArrayList<>();
        String descripcion = "Consumo: " + request.cantidad() + "x " + plato.getNombre();

        for (PlatoIngrediente pi : plato.getReceta()) {
            BigDecimal cantidadTotal = pi.getCantidad()
                .multiply(BigDecimal.valueOf(request.cantidad()));
            Ingrediente ingrediente = pi.getIngrediente();

            ingrediente.setStockActual(ingrediente.getStockActual().subtract(cantidadTotal));
            ingredienteRepo.save(ingrediente);

            MovimientoStock movimiento = new MovimientoStock();
            movimiento.setBar(bar);
            movimiento.setIngrediente(ingrediente);
            movimiento.setTipo(TipoMovimiento.CONSUMO);
            movimiento.setCantidad(cantidadTotal);
            movimiento.setDescripcion(descripcion);

            movimiento = movimientoRepo.save(movimiento);
            movimientos.add(MovimientoStockResponse.desdeEntidad(movimiento));
        }

        return movimientos;
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockResponse> listarMovimientos(Long barId) {
        return movimientoRepo.findByBarIdOrderByCreatedAtDesc(barId).stream()
            .map(MovimientoStockResponse::desdeEntidad)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockResponse> listarMovimientosPorIngrediente(
            Long barId, Long ingredienteId) {
        return movimientoRepo
            .findByBarIdAndIngredienteIdOrderByCreatedAtDesc(barId, ingredienteId).stream()
            .map(MovimientoStockResponse::desdeEntidad)
            .toList();
    }

    @Transactional(readOnly = true)
    public String exportarCsv(Long barId) {
        List<MovimientoStock> consumos = movimientoRepo
            .findByBarIdAndTipoOrderByCreatedAtAsc(barId, TipoMovimiento.CONSUMO);

        StringBuilder csv = new StringBuilder();
        csv.append("fecha,ingrediente_id,nombre_ingrediente,cantidad\n");

        for (MovimientoStock m : consumos) {
            csv.append(m.getCreatedAt().toLocalDate())
               .append(",")
               .append(m.getIngrediente().getId())
               .append(",")
               .append(m.getIngrediente().getNombre())
               .append(",")
               .append(m.getCantidad())
               .append("\n");
        }

        return csv.toString();
    }
}