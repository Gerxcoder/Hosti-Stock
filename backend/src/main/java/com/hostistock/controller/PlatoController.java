package com.hostistock.controller;

import com.hostistock.dto.DisponibilidadResponse;
import com.hostistock.dto.PlatoRequest;
import com.hostistock.dto.PlatoResponse;
import com.hostistock.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/platos")
public class PlatoController {

    private final PlatoService platoService;

    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    @GetMapping
    public ResponseEntity<List<PlatoResponse>> listar() {
        return ResponseEntity.ok(platoService.listar(obtenerBarId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(platoService.obtenerPorId(obtenerBarId(), id));
    }

    @PostMapping
    public ResponseEntity<PlatoResponse> crear(@Valid @RequestBody PlatoRequest request) {
        PlatoResponse response = platoService.crear(obtenerBarId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlatoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlatoRequest request) {
        return ResponseEntity.ok(platoService.actualizar(obtenerBarId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        platoService.eliminar(obtenerBarId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> disponibilidadPlato(@PathVariable Long id) {
        return ResponseEntity.ok(platoService.calcularDisponibilidad(obtenerBarId(), id));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> disponibilidadTodos() {
        return ResponseEntity.ok(platoService.listarDisponibilidad(obtenerBarId()));
    }

    private Long obtenerBarId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}