package com.hostistock.desktop.controller;

import com.hostistock.desktop.service.ApiClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class PrediccionesController {

    private TableView<Map<String, Object>> tablaPredicciones;
    private ListView<String> listaRecomendaciones;
    private Label lblEstado;

    @SuppressWarnings("unchecked")
    public VBox crearVista() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titulo = new Label("Inteligencia Artificial – Predicciones");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Controles ──
        GridPane controles = new GridPane();
        controles.setHgap(10);
        controles.setVgap(8);
        controles.setPadding(new Insets(12));
        controles.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        controles.setAlignment(Pos.CENTER_LEFT);

        Label lblDias = new Label("Días a predecir:");
        lblDias.setMinWidth(110);

        TextField txtDias = new TextField("7");
        txtDias.setPrefWidth(60);

        Button btnEntrenar = new Button("Entrenar Modelo");
        btnEntrenar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; "
            + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 8 14;");

        Button btnPredecir = new Button("Generar Predicciones");
        btnPredecir.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; "
            + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 8 14;");

        Button btnRecomendaciones = new Button("Ver Recomendaciones de Compra");
        btnRecomendaciones.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
            + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 8 14;");

        controles.add(lblDias,           0, 0);
        controles.add(txtDias,           1, 0);
        controles.add(btnEntrenar,       2, 0);
        controles.add(btnPredecir,       3, 0);
        controles.add(btnRecomendaciones,4, 0);

        // ── Tabla de predicciones ──
        Label lblPredicciones = new Label("Predicciones de Consumo");
        lblPredicciones.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tablaPredicciones = new TableView<>();
        tablaPredicciones.setPrefHeight(200);

        TableColumn<Map<String, Object>, Object> colIng = new TableColumn<>("Ingrediente");
        colIng.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombreIngrediente")));
        colIng.setPrefWidth(150);

        TableColumn<Map<String, Object>, Object> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("fechaPrediccion")));
        colFecha.setPrefWidth(120);

        TableColumn<Map<String, Object>, Object> colConsumo = new TableColumn<>("Consumo Previsto");
        colConsumo.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("consumoPrevisto")));
        colConsumo.setPrefWidth(140);

        TableColumn<Map<String, Object>, Object> colStock = new TableColumn<>("Stock Estimado");
        colStock.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("stockEstimado")));
        colStock.setPrefWidth(130);

        TableColumn<Map<String, Object>, Object> colRec = new TableColumn<>("Compra Recomendada");
        colRec.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("recomendacionCompra")));
        colRec.setPrefWidth(150);

        tablaPredicciones.getColumns().addAll(colIng, colFecha, colConsumo, colStock, colRec);

        // ── Recomendaciones de compra ──
        Label lblRecomendaciones = new Label("Recomendaciones de Compra");
        lblRecomendaciones.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

        listaRecomendaciones = new ListView<>();
        listaRecomendaciones.setPrefHeight(150);

        lblEstado = new Label();
        lblEstado.setStyle("-fx-font-size: 12px;");
        lblEstado.setWrapText(true);

        // ── Eventos ──
        btnEntrenar.setOnAction(e -> {
            mostrarMensaje("Entrenando modelo... (puede tardar unos segundos)", false);
            new Thread(() -> {
                try {
                    Map<String, Object> resp = ApiClient.obtenerInstancia()
                        .postSinCuerpo("/api/ia/entrenar");
                    javafx.application.Platform.runLater(() ->
                        mostrarMensaje("" + resp.get("mensaje"), false));
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() ->
                        mostrarMensaje("Error: " + ex.getMessage(), true));
                }
            }).start();
        });

        btnPredecir.setOnAction(e -> {
            try {
                int dias = Integer.parseInt(txtDias.getText());
                List<Map<String, Object>> predicciones = ApiClient.obtenerInstancia()
                    .getList("/api/ia/predicciones?dias=" + dias);
                ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(predicciones);
                tablaPredicciones.setItems(items);
                mostrarMensaje("Predicciones cargadas: " + predicciones.size() + " registros", false);
            } catch (Exception ex) {
                mostrarMensaje("Error: " + ex.getMessage(), true);
            }
        });

        btnRecomendaciones.setOnAction(e -> {
            try {
                int dias = Integer.parseInt(txtDias.getText());
                List<Map<String, Object>> recs = ApiClient.obtenerInstancia()
                    .getList("/api/ia/recomendaciones-compra?dias=" + dias);
                listaRecomendaciones.getItems().clear();
                for (Map<String, Object> rec : recs) {
                    String alerta = (String) rec.get("alerta");
                    if (alerta != null) {
                        listaRecomendaciones.getItems().add(alerta);
                    } else {
                        listaRecomendaciones.getItems().add(String.format(
                            "OK: %s – stock estimado: %s %s",
                            rec.get("nombreIngrediente"),
                            rec.get("stockEstimadoTrasPeriodo"),
                            rec.get("unidad")
                        ));
                    }
                }
                mostrarMensaje("Recomendaciones cargadas", false);
            } catch (Exception ex) {
                mostrarMensaje("Error: " + ex.getMessage(), true);
            }
        });

        root.getChildren().addAll(
            titulo, controles,
            lblPredicciones, tablaPredicciones,
            lblRecomendaciones, listaRecomendaciones,
            lblEstado
        );
        return root;
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
            (esError ? "#e74c3c" : "#27ae60") + ";");
    }
}
