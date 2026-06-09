package com.hostistock.service;

import com.hostistock.dto.*;
import com.hostistock.model.*;
import com.hostistock.exception.RecursoNoEncontradoException;
import com.hostistock.repository.BarRepository;
import com.hostistock.repository.IngredienteRepository;
import com.hostistock.repository.PlatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// Disponibilidad = MIN(stock/cantidad) por ingrediente. Ej: tortilla 200g patata + 3 huevos → 3 uds si hay 1kg y 10 huevos.
@Service
public class PlatoService {

    private final PlatoRepository platoRepo;
    private final BarRepository barRepo;
    private final IngredienteRepository ingredienteRepo;

    public PlatoService(PlatoRepository platoRepo,
                         BarRepository barRepo,
                         IngredienteRepository ingredienteRepo) {
        this.platoRepo = platoRepo;
        this.barRepo = barRepo;
        this.ingredienteRepo = ingredienteRepo;
    }

    public List<PlatoResponse> listar(Long barId) {
        return platoRepo.findByBarId(barId).stream()
            .map(plato -> PlatoResponse.desdeEntidad(plato, calcularUnidadesDisponibles(plato)))
            .toList();
    }

    public PlatoResponse obtenerPorId(Long barId, Long id) {
        Plato plato = buscarPlato(barId, id);
        return PlatoResponse.desdeEntidad(plato, calcularUnidadesDisponibles(plato));
    }

    @Transactional
    public PlatoResponse crear(Long barId, PlatoRequest request) {
        if (platoRepo.existsByNombreAndBarId(request.nombre(), barId)) {
            throw new IllegalArgumentException(
                "Ya existe un plato con el nombre '" + request.nombre() + "' en este bar");
        }

        Bar bar = barRepo.findById(barId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bar no encontrado"));

        Plato plato = new Plato();
        plato.setBar(bar);
        plato.setNombre(request.nombre());
        plato.setCategoria(request.categoria());

        // Asignar receta si se proporcionó
        if (request.receta() != null && !request.receta().isEmpty()) {
            asignarReceta(plato, request.receta(), barId);
        }

        plato = platoRepo.save(plato);
        return PlatoResponse.desdeEntidad(plato, calcularUnidadesDisponibles(plato));
    }

    // orphanRemoval=true en Plato se encarga de borrar las líneas antiguas de receta al hacer clear()
    @Transactional
    public PlatoResponse actualizar(Long barId, Long id, PlatoRequest request) {
        Plato plato = buscarPlato(barId, id);

        plato.setNombre(request.nombre());
        plato.setCategoria(request.categoria());

        // Limpiar receta actual y asignar la nueva
        plato.getReceta().clear();
        if (request.receta() != null && !request.receta().isEmpty()) {
            asignarReceta(plato, request.receta(), barId);
        }

        plato = platoRepo.save(plato);
        return PlatoResponse.desdeEntidad(plato, calcularUnidadesDisponibles(plato));
    }

    @Transactional
    public void eliminar(Long barId, Long id) {
        Plato plato = buscarPlato(barId, id);
        platoRepo.delete(plato);
    }

    public DisponibilidadResponse calcularDisponibilidad(Long barId, Long platoId) {
        Plato plato = buscarPlato(barId, platoId);
        int disponibles = calcularUnidadesDisponibles(plato);
        return new DisponibilidadResponse(plato.getId(), plato.getNombre(), disponibles);
    }

    public List<DisponibilidadResponse> listarDisponibilidad(Long barId) {
        return platoRepo.findByBarId(barId).stream()
            .map(plato -> new DisponibilidadResponse(
                plato.getId(),
                plato.getNombre(),
                calcularUnidadesDisponibles(plato)))
            .toList();
    }

    // ─── auxiliares ──────────────────────────────────────────────────────────

    private int calcularUnidadesDisponibles(Plato plato) {
        if (plato.getReceta() == null || plato.getReceta().isEmpty()) {
            return 0;
        }

        int minimo = Integer.MAX_VALUE;
        for (PlatoIngrediente pi : plato.getReceta()) {
            BigDecimal stock = pi.getIngrediente().getStockActual();
            BigDecimal cantidad = pi.getCantidad();
            int disponibles = stock.divide(cantidad, 0, RoundingMode.FLOOR).intValue();
            minimo = Math.min(minimo, disponibles);
        }

        return minimo == Integer.MAX_VALUE ? 0 : minimo;
    }

    private void asignarReceta(Plato plato, List<RecetaItemRequest> items, Long barId) {
        List<PlatoIngrediente> receta = new ArrayList<>();
        for (RecetaItemRequest item : items) {
            Ingrediente ingrediente = ingredienteRepo.findByIdAndBarId(item.ingredienteId(), barId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "No se encontró el ingrediente con ID " + item.ingredienteId()));

            PlatoIngrediente pi = new PlatoIngrediente();
            pi.setPlato(plato);
            pi.setIngrediente(ingrediente);
            pi.setCantidad(item.cantidad());
            receta.add(pi);
        }
        plato.getReceta().addAll(receta);
    }

    private Plato buscarPlato(Long barId, Long platoId) {
        return platoRepo.findByIdAndBarId(platoId, barId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el plato con ID " + platoId));
    }
}