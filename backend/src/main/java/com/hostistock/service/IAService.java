package com.hostistock.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostistock.dto.PrediccionResponse;
import com.hostistock.dto.RecomendacionCompraResponse;
import com.hostistock.model.*;
import com.hostistock.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// app.ia.url vacío → ProcessBuilder (Python local). Con URL → HTTP al microservicio FastAPI.
@Service
public class IAService {

    @Value("${app.ia.python-path}")
    private String pythonPath;

    @Value("${app.ia.directorio}")
    private String directorioIA;

    @Value("${app.ia.url:}")
    private String iaUrl;

    private final ConsumoService consumoService;
    private final PrediccionRepository prediccionRepo;
    private final IngredienteRepository ingredienteRepo;
    private final BarRepository barRepo;
    private final ObjectMapper objectMapper;

    public IAService(ConsumoService consumoService,
                      PrediccionRepository prediccionRepo,
                      IngredienteRepository ingredienteRepo,
                      BarRepository barRepo,
                      ObjectMapper objectMapper) {
        this.consumoService = consumoService;
        this.prediccionRepo = prediccionRepo;
        this.ingredienteRepo = ingredienteRepo;
        this.barRepo = barRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public String entrenar(Long barId) {
        try {
            String csv = consumoService.exportarCsv(barId);

            if (usarMicroservicio()) {
                return entrenarViaHttp(barId, csv);
            }
            return entrenarViaProcessBuilder(barId, csv);

        } catch (Exception e) {
            return "Error al ejecutar entrenamiento: " + e.getMessage();
        }
    }

    @Transactional
    public List<PrediccionResponse> predecir(Long barId, int dias) {
        try {
            String salida = usarMicroservicio()
                ? predecirViaHttp(barId, dias)
                : predecirViaProcessBuilder(barId, dias);

            List<Map<String, Object>> prediccionesJson = objectMapper.readValue(
                salida, new TypeReference<>() {});

            // Borra las predicciones antiguas y guarda las nuevas
            prediccionRepo.deleteByBarId(barId);

            Bar bar = barRepo.findById(barId)
                .orElseThrow(() -> new RuntimeException("Bar no encontrado"));

            List<Prediccion> predicciones = new ArrayList<>();
            for (Map<String, Object> pred : prediccionesJson) {
                Long ingredienteId = ((Number) pred.get("ingrediente_id")).longValue();
                Ingrediente ingrediente = ingredienteRepo.findByIdAndBarId(ingredienteId, barId)
                    .orElse(null);
                if (ingrediente == null) continue;

                Prediccion p = new Prediccion();
                p.setBar(bar);
                p.setIngrediente(ingrediente);
                p.setFechaPrediccion(LocalDate.parse((String) pred.get("fecha")));
                p.setConsumoPrevisto(new BigDecimal(pred.get("consumo_previsto").toString()));
                p.setStockEstimado(new BigDecimal(pred.get("stock_estimado").toString()));
                p.setRecomendacionCompra(new BigDecimal(pred.get("recomendacion_compra").toString()));

                predicciones.add(p);
            }

            prediccionRepo.saveAll(predicciones);

            return predicciones.stream()
                .map(PrediccionResponse::desdeEntidad)
                .toList();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar predicción: " + e.getMessage(), e);
        }
    }

    public List<PrediccionResponse> obtenerPredicciones(Long barId, int dias) {
        LocalDate desde = LocalDate.now();
        LocalDate hasta = desde.plusDays(dias);

        return prediccionRepo
            .findByBarIdAndFechaPrediccionBetweenOrderByFechaPrediccionAsc(barId, desde, hasta)
            .stream()
            .map(PrediccionResponse::desdeEntidad)
            .toList();
    }

    public List<RecomendacionCompraResponse> recomendacionesCompra(Long barId, int dias) {
        LocalDate desde = LocalDate.now();
        LocalDate hasta = desde.plusDays(dias);

        List<Prediccion> predicciones = prediccionRepo
            .findByBarIdAndFechaPrediccionBetweenOrderByFechaPrediccionAsc(barId, desde, hasta);

        Map<Long, BigDecimal> consumoPorIngrediente = predicciones.stream()
            .collect(Collectors.groupingBy(
                p -> p.getIngrediente().getId(),
                Collectors.reducing(BigDecimal.ZERO,
                    Prediccion::getConsumoPrevisto,
                    BigDecimal::add)));

        List<Ingrediente> ingredientes = ingredienteRepo.findByBarId(barId);
        List<RecomendacionCompraResponse> recomendaciones = new ArrayList<>();

        for (Ingrediente ing : ingredientes) {
            BigDecimal consumoTotal = consumoPorIngrediente
                .getOrDefault(ing.getId(), BigDecimal.ZERO);
            BigDecimal stockEstimado = ing.getStockActual().subtract(consumoTotal);
            BigDecimal cantidadRecomendada = BigDecimal.ZERO;
            String alerta = null;

            if (stockEstimado.compareTo(ing.getStockMinimo()) < 0) {
                cantidadRecomendada = ing.getStockMinimo().multiply(BigDecimal.valueOf(2))
                    .subtract(stockEstimado);
                if (cantidadRecomendada.compareTo(BigDecimal.ZERO) < 0) {
                    cantidadRecomendada = BigDecimal.ZERO;
                }
                alerta = "STOCK BAJO: se recomienda comprar " + cantidadRecomendada +
                    " " + ing.getUnidad() + " de " + ing.getNombre();
            }

            recomendaciones.add(new RecomendacionCompraResponse(
                ing.getId(),
                ing.getNombre(),
                ing.getUnidad(),
                ing.getStockActual(),
                consumoTotal,
                stockEstimado,
                cantidadRecomendada,
                alerta
            ));
        }

        return recomendaciones;
    }

    // ─── privados ─────────────────────────────────────────────────────────────

    private boolean usarMicroservicio() {
        return iaUrl != null && !iaUrl.isBlank();
    }

    private String entrenarViaHttp(Long barId, String csv) throws Exception {
        Map<String, Object> body = Map.of("bar_id", barId, "csv_content", csv);
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(iaUrl + "/train"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return "Error en entrenamiento (HTTP " + response.statusCode() + "): " + response.body();
        }
        return "Entrenamiento completado. " + response.body();
    }

    private String predecirViaHttp(Long barId, int dias) throws Exception {
        Map<String, Object> body = Map.of("bar_id", barId, "dias", dias);
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(iaUrl + "/predict"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error en predicción (HTTP " + response.statusCode() + "): " + response.body());
        }
        return response.body();
    }

    private String entrenarViaProcessBuilder(Long barId, String csv) throws Exception {
        Path csvPath = Paths.get(directorioIA, "data", "consumos_bar_" + barId + ".csv");
        Files.createDirectories(csvPath.getParent());
        Files.writeString(csvPath, csv);

        ProcessBuilder pb = new ProcessBuilder(
            pythonPath,
            Paths.get(directorioIA, "entrenar.py").toString(),
            "--bar_id", barId.toString(),
            "--csv", csvPath.toString()
        );
        pb.directory(Paths.get(directorioIA).toFile());
        pb.redirectErrorStream(true);

        Process proceso = pb.start();
        String salida = leerSalidaProceso(proceso);
        int codigoSalida = proceso.waitFor();

        if (codigoSalida != 0) {
            return "Error en entrenamiento (código " + codigoSalida + "): " + salida;
        }
        return "Entrenamiento completado. " + salida;
    }

    private String predecirViaProcessBuilder(Long barId, int dias) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            pythonPath,
            Paths.get(directorioIA, "predecir.py").toString(),
            "--bar_id", barId.toString(),
            "--dias", String.valueOf(dias)
        );
        pb.directory(Paths.get(directorioIA).toFile());
        pb.redirectErrorStream(true);

        Process proceso = pb.start();
        String salida = leerSalidaProceso(proceso);
        int codigoSalida = proceso.waitFor();

        if (codigoSalida != 0) {
            throw new RuntimeException("Error en predicción: " + salida);
        }
        return salida;
    }

    private String leerSalidaProceso(Process proceso) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proceso.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
