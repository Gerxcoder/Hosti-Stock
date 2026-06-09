package com.hostistock.service;

import com.hostistock.dto.IngredienteRequest;
import com.hostistock.dto.IngredienteResponse;
import com.hostistock.dto.MovimientoStockRequest;
import com.hostistock.dto.MovimientoStockResponse;
import com.hostistock.model.*;
import com.hostistock.exception.RecursoNoEncontradoException;
import com.hostistock.exception.StockInsuficienteException;
import com.hostistock.repository.BarRepository;
import com.hostistock.repository.IngredienteRepository;
import com.hostistock.repository.MovimientoStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class IngredienteService {
    private final IngredienteRepository ingredienteRepo;
    private final BarRepository barRepo;
    private final MovimientoStockRepository movimientoRepo;

    public IngredienteService(IngredienteRepository ingredienteRepo,
                               BarRepository barRepo,
                               MovimientoStockRepository movimientoRepo) {
        this.ingredienteRepo = ingredienteRepo;
        this.barRepo = barRepo;
        this.movimientoRepo = movimientoRepo;
    }

    public List<IngredienteResponse> listar(Long barId) {
        return ingredienteRepo.findByBarId(barId).stream()
            .map(IngredienteResponse::desdeEntidad)
            .toList();
    }

    public IngredienteResponse obtenerPorId(Long barId, Long id) {
        Ingrediente ingrediente = buscarIngrediente(barId, id);
        return IngredienteResponse.desdeEntidad(ingrediente);
    }

    @Transactional
    public IngredienteResponse crear(Long barId, IngredienteRequest request) {
        if (ingredienteRepo.existsByNombreAndBarId(request.nombre(), barId)) {
            throw new IllegalArgumentException(
                "Ya existe un ingrediente con el nombre '" + request.nombre() + "' en este bar");
        }

        Bar bar = barRepo.findById(barId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bar no encontrado"));

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setBar(bar);
        ingrediente.setNombre(request.nombre());
        ingrediente.setUnidad(request.unidad());
        ingrediente.setStockActual(request.stockActual());
        ingrediente.setStockMinimo(request.stockMinimo());

        ingrediente = ingredienteRepo.save(ingrediente);
        return IngredienteResponse.desdeEntidad(ingrediente);
    }

    @Transactional
    public IngredienteResponse actualizar(Long barId, Long id, IngredienteRequest request) {
        Ingrediente ingrediente = buscarIngrediente(barId, id);

        ingrediente.setNombre(request.nombre());
        ingrediente.setUnidad(request.unidad());
        ingrediente.setStockActual(request.stockActual());
        ingrediente.setStockMinimo(request.stockMinimo());

        ingrediente = ingredienteRepo.save(ingrediente);
        return IngredienteResponse.desdeEntidad(ingrediente);
    }

    @Transactional
    public void eliminar(Long barId, Long id) {
        Ingrediente ingrediente = buscarIngrediente(barId, id);
        ingredienteRepo.delete(ingrediente);
    }

    @Transactional
    public MovimientoStockResponse ajustarStock(Long barId, MovimientoStockRequest request) {
        Ingrediente ingrediente = buscarIngrediente(barId, request.ingredienteId());
        Bar bar = barRepo.findById(barId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bar no encontrado"));

        BigDecimal nuevoStock;
        if (request.tipo() == TipoMovimiento.ENTRADA) {
            nuevoStock = ingrediente.getStockActual().add(request.cantidad());
        } else {
            nuevoStock = ingrediente.getStockActual().subtract(request.cantidad());
            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new StockInsuficienteException(
                    "Stock insuficiente de '" + ingrediente.getNombre() +
                    "': disponible=" + ingrediente.getStockActual() +
                    ", solicitado=" + request.cantidad());
            }
        }

        ingrediente.setStockActual(nuevoStock);
        ingredienteRepo.save(ingrediente);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setBar(bar);
        movimiento.setIngrediente(ingrediente);
        movimiento.setTipo(request.tipo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setDescripcion(request.descripcion());

        movimiento = movimientoRepo.save(movimiento);
        return MovimientoStockResponse.desdeEntidad(movimiento);
    }

    public List<IngredienteResponse> listarStockBajo(Long barId) {
        return ingredienteRepo.findByBarId(barId).stream()
            .filter(i -> i.getStockActual().compareTo(i.getStockMinimo()) < 0)
            .map(IngredienteResponse::desdeEntidad)
            .toList();
    }

    private Ingrediente buscarIngrediente(Long barId, Long ingredienteId) {
        return ingredienteRepo.findByIdAndBarId(ingredienteId, barId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el ingrediente con ID " + ingredienteId));
    }
}