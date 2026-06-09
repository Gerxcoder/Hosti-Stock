package com.hostistock.controller;

import com.hostistock.dto.ConsumoRequest;
import com.hostistock.dto.MovimientoStockResponse;
import com.hostistock.service.ConsumoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consumos")
public class ConsumoController {

    private final ConsumoService consumoService;

    public ConsumoController(ConsumoService consumoService) {
        this.consumoService = consumoService;
    }

    @PostMapping
    public ResponseEntity<List<MovimientoStockResponse>> registrarConsumo(
            @Valid @RequestBody ConsumoRequest request) {
        List<MovimientoStockResponse> movimientos =
            consumoService.registrarConsumo(obtenerBarId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientos);
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoStockResponse>> listarMovimientos() {
        return ResponseEntity.ok(consumoService.listarMovimientos(obtenerBarId()));
    }

    @GetMapping("/movimientos/ingrediente/{id}")
    public ResponseEntity<List<MovimientoStockResponse>> movimientosPorIngrediente(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            consumoService.listarMovimientosPorIngrediente(obtenerBarId(), id));
    }

    @GetMapping(value = "/export-csv", produces = "text/csv")
    public ResponseEntity<String> exportarCsv() {
        String csv = consumoService.exportarCsv(obtenerBarId());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header("Content-Disposition", "attachment; filename=consumos.csv")
            .body(csv);
    }

    private Long obtenerBarId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}