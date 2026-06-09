package com.hostistock.desktop.controller;

import com.hostistock.desktop.service.ApiClient;
import com.hostistock.desktop.service.SesionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;

public class DashboardController {

    public VBox crearVista() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(10));

        Label titulo = new Label("Dashboard – " + SesionManager.obtenerInstancia().getNombreBar());
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Sección: Stock bajo ──
        Label lblStockBajo = new Label("Alertas de Stock Bajo");
        lblStockBajo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        ListView<String> listaStockBajo = new ListView<>();
        listaStockBajo.setPrefHeight(200);

        // ── Sección: Disponibilidad ──
        Label lblDisponibilidad = new Label("Disponibilidad de Platos");
        lblDisponibilidad.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        ListView<String> listaDisponibilidad = new ListView<>();
        listaDisponibilidad.setPrefHeight(200);

        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnRefrescar.setOnAction(e -> cargarDatos(listaStockBajo, listaDisponibilidad));

        root.getChildren().addAll(
            titulo,
            lblStockBajo, listaStockBajo,
            lblDisponibilidad, listaDisponibilidad,
            btnRefrescar
        );

        // Cargar datos al mostrar
        cargarDatos(listaStockBajo, listaDisponibilidad);

        return root;
    }

    private void cargarDatos(ListView<String> listaStock, ListView<String> listaDispo) {
        ApiClient api = ApiClient.obtenerInstancia();

        try {
            // Stock bajo
            List<Map<String, Object>> stockBajo = api.getList("/api/ingredientes/stock-bajo");
            listaStock.getItems().clear();
            if (stockBajo.isEmpty()) {
                listaStock.getItems().add("Sin alertas de stock bajo");
            } else {
                for (Map<String, Object> ing : stockBajo) {
                    listaStock.getItems().add(String.format(
                        "⚠ %s: %s %s (mínimo: %s)",
                        ing.get("nombre"),
                        ing.get("stockActual"),
                        ing.get("unidad"),
                        ing.get("stockMinimo")
                    ));
                }
            }

            // Disponibilidad de platos
            List<Map<String, Object>> disponibilidad = api.getList("/api/platos/disponibilidad");
            listaDispo.getItems().clear();
            if (disponibilidad.isEmpty()) {
                listaDispo.getItems().add("No hay platos registrados");
            } else {
                for (Map<String, Object> plato : disponibilidad) {
                    int uds = ((Number) plato.get("unidadesDisponibles")).intValue();
                    String indicador = uds == 0 ? "✗" : "✓";
                    listaDispo.getItems().add(String.format(
                        "%s %s: %d unidades disponibles",
                        indicador, plato.get("nombrePlato"), uds
                    ));
                }
            }

        } catch (Exception e) {
            listaStock.getItems().clear();
            listaStock.getItems().add("Error al cargar datos: " + e.getMessage());
        }
    }
}
