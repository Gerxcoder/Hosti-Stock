package com.hostistock.controller;

import com.hostistock.dto.IngredienteRequest;
import com.hostistock.dto.IngredienteResponse;
import com.hostistock.dto.MovimientoStockRequest;
import com.hostistock.dto.MovimientoStockResponse;
import com.hostistock.service.IngredienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ingredientes")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    public IngredienteController(IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    @GetMapping
    public ResponseEntity<List<IngredienteResponse>> listar() {
        return ResponseEntity.ok(ingredienteService.listar(obtenerBarId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredienteResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ingredienteService.obtenerPorId(obtenerBarId(), id));
    }

    @PostMapping
    public ResponseEntity<IngredienteResponse> crear(
            @Valid @RequestBody IngredienteRequest request) {
        IngredienteResponse response = ingredienteService.crear(obtenerBarId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.ok(ingredienteService.actualizar(obtenerBarId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ingredienteService.eliminar(obtenerBarId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movimiento")
    public ResponseEntity<MovimientoStockResponse> ajustarStock(
            @Valid @RequestBody MovimientoStockRequest request) {
        MovimientoStockResponse response = ingredienteService.ajustarStock(obtenerBarId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<IngredienteResponse>> listarStockBajo() {
        return ResponseEntity.ok(ingredienteService.listarStockBajo(obtenerBarId()));
    }

    private Long obtenerBarId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}