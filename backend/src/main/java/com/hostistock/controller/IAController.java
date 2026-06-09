package com.hostistock.controller;

import com.hostistock.dto.PrediccionResponse;
import com.hostistock.dto.RecomendacionCompraResponse;
import com.hostistock.service.IAService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    @PostMapping("/entrenar")
    public ResponseEntity<Map<String, String>> entrenar() {
        String resultado = iaService.entrenar(obtenerBarId());
        return ResponseEntity.ok(Map.of("mensaje", resultado));
    }

    @GetMapping("/predicciones")
    public ResponseEntity<List<PrediccionResponse>> predicciones(
            @RequestParam(defaultValue = "7") int dias) {
        List<PrediccionResponse> resultado = iaService.predecir(obtenerBarId(), dias);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/recomendaciones-compra")
    public ResponseEntity<List<RecomendacionCompraResponse>> recomendacionesCompra(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(iaService.recomendacionesCompra(obtenerBarId(), dias));
    }

    private Long obtenerBarId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}