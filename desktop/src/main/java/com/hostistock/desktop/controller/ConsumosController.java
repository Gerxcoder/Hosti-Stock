package com.hostistock.desktop.controller;

import com.hostistock.desktop.service.ApiClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import java.util.*;

public class ConsumosController {

    private TableView<Map<String, Object>> tablaMovimientos;
    private Label lblEstado;

    @SuppressWarnings("unchecked")
    public VBox crearVista() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titulo = new Label("Registro de Consumos");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Formulario de consumo ──
        GridPane formConsumo = new GridPane();
        formConsumo.setHgap(10);
        formConsumo.setVgap(8);
        formConsumo.setPadding(new Insets(12));
        formConsumo.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 6;");
        formConsumo.setAlignment(Pos.CENTER_LEFT);

        TextField txtPlatoId = new TextField();
        txtPlatoId.setPromptText("ID del plato");
        txtPlatoId.setPrefWidth(130);

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Unidades");
        txtCantidad.setPrefWidth(110);

        Button btnRegistrar = new Button("Registrar Consumo");
        btnRegistrar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; "
            + "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 8 16;");

        Label lblPlatoId = new Label("Plato ID:");
        lblPlatoId.setMinWidth(70);
        Label lblCantidad = new Label("Cantidad:");
        lblCantidad.setMinWidth(70);

        formConsumo.add(lblPlatoId,   0, 0); formConsumo.add(txtPlatoId,  1, 0);
        formConsumo.add(lblCantidad,  2, 0); formConsumo.add(txtCantidad, 3, 0);
        formConsumo.add(btnRegistrar, 4, 0);

        txtCantidad.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) btnRegistrar.fire(); });

        // ── Historial de movimientos ──
        Label lblHistorial = new Label("Historial de Movimientos de Stock");
        lblHistorial.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tablaMovimientos = new TableView<>();
        tablaMovimientos.setPrefHeight(350);

        TableColumn<Map<String, Object>, Object> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("id")));
        colId.setPrefWidth(50);

        TableColumn<Map<String, Object>, Object> colIngrediente = new TableColumn<>("Ingrediente");
        colIngrediente.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombreIngrediente")));
        colIngrediente.setPrefWidth(150);

        TableColumn<Map<String, Object>, Object> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("tipo")));
        colTipo.setPrefWidth(100);

        TableColumn<Map<String, Object>, Object> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("cantidad")));
        colCantidad.setPrefWidth(100);

        TableColumn<Map<String, Object>, Object> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("descripcion")));
        colDesc.setPrefWidth(200);

        TableColumn<Map<String, Object>, Object> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("createdAt")));
        colFecha.setPrefWidth(160);

        tablaMovimientos.getColumns().addAll(colId, colIngrediente, colTipo, colCantidad, colDesc, colFecha);

        lblEstado = new Label();
        lblEstado.setStyle("-fx-font-size: 12px;");

        // ── Eventos ──
        btnRegistrar.setOnAction(e -> {
            try {
                Map<String, Object> datos = new HashMap<>();
                datos.put("platoId", Long.parseLong(txtPlatoId.getText()));
                datos.put("cantidad", Integer.parseInt(txtCantidad.getText()));

                ApiClient.obtenerInstancia().postList("/api/consumos", datos);
                txtPlatoId.clear(); txtCantidad.clear();
                mostrarMensaje("Consumo registrado. Stock actualizado.", false);
                cargarHistorial();
            } catch (Exception ex) {
                mostrarMensaje("Error: " + ex.getMessage(), true);
            }
        });

        Button btnRefrescar = new Button("Refrescar Historial");
        btnRefrescar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnRefrescar.setOnAction(e -> cargarHistorial());

        root.getChildren().addAll(titulo, formConsumo, lblHistorial, tablaMovimientos, btnRefrescar, lblEstado);
        cargarHistorial();
        return root;
    }

    private void cargarHistorial() {
        try {
            List<Map<String, Object>> datos = ApiClient.obtenerInstancia()
                .getList("/api/consumos/movimientos");
            ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(datos);
            tablaMovimientos.setItems(items);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar historial: " + e.getMessage(), true);
        }
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
            (esError ? "#e74c3c" : "#27ae60") + ";");
    }
}
