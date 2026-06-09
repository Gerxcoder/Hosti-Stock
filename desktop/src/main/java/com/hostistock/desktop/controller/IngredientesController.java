package com.hostistock.desktop.controller;

import com.hostistock.desktop.service.ApiClient;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.*;

public class IngredientesController {

    private TableView<Map<String, Object>> tabla;
    private Label lblEstado;
    private Label lblIngSeleccionado;
    private ComboBox<String> cmbTipo;
    private TextField txtCantidad;
    private TextField txtDescripcion;
    private Button btnAjustar;

    @SuppressWarnings("unchecked")
    public VBox crearVista() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titulo = new Label("Gestión de Ingredientes");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Tabla ──
        tabla = new TableView<>();
        tabla.setPrefHeight(340);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay ingredientes todavía. Pulsa \"+ Nuevo Ingrediente\" para añadir."));

        TableColumn<Map<String, Object>, Object> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("id")));
        colId.setMaxWidth(55);
        colId.setMinWidth(55);

        TableColumn<Map<String, Object>, Object> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombre")));

        TableColumn<Map<String, Object>, Object> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("unidad")));
        colUnidad.setPrefWidth(100);

        TableColumn<Map<String, Object>, Object> colStock = new TableColumn<>("Stock Actual");
        colStock.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("stockActual")));
        colStock.setPrefWidth(110);

        TableColumn<Map<String, Object>, Object> colMinimo = new TableColumn<>("Stock Mínimo");
        colMinimo.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("stockMinimo")));
        colMinimo.setPrefWidth(110);

        TableColumn<Map<String, Object>, Object> colAlerta = new TableColumn<>("⚠ Stock bajo");
        colAlerta.setCellValueFactory(cd -> new SimpleObjectProperty<>(
            Boolean.TRUE.equals(cd.getValue().get("stockBajo")) ? "⚠ SÍ" : ""));
        colAlerta.setPrefWidth(95);

        tabla.getColumns().addAll(colId, colNombre, colUnidad, colStock, colMinimo, colAlerta);
        tabla.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nuevo) -> actualizarPanelAjuste(nuevo));

        // ── Botones principales ──
        HBox botones = new HBox(10);
        botones.setPadding(new Insets(4, 0, 4, 0));

        Button btnNuevo = new Button("+ Nuevo Ingrediente");
        btnNuevo.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");

        Button btnEditar = new Button("Editar seleccionado");
        btnEditar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");

        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");

        botones.getChildren().addAll(btnNuevo, btnEditar, btnEliminar);

        // ── Panel de ajuste de stock ──
        VBox panelAjuste = new VBox(8);
        panelAjuste.setPadding(new Insets(10));
        panelAjuste.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: #f8f9fa; -fx-background-radius: 6;");

        Label lblAjusteTitulo = new Label("Ajustar stock");
        lblAjusteTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        lblIngSeleccionado = new Label("← Selecciona un ingrediente de la tabla");
        lblIngSeleccionado.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        HBox formAjuste = new HBox(10);
        formAjuste.setAlignment(Pos.CENTER_LEFT);

        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("ENTRADA", "SALIDA");
        cmbTipo.setPromptText("Tipo");
        cmbTipo.setPrefWidth(120);

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");
        txtCantidad.setPrefWidth(100);

        txtDescripcion = new TextField();
        txtDescripcion.setPromptText("Descripción (opcional)");
        HBox.setHgrow(txtDescripcion, Priority.ALWAYS);

        btnAjustar = new Button("Ajustar Stock");
        btnAjustar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 7 14;");
        btnAjustar.setDisable(true);

        formAjuste.getChildren().addAll(cmbTipo, txtCantidad, txtDescripcion, btnAjustar);
        panelAjuste.getChildren().addAll(lblAjusteTitulo, lblIngSeleccionado, formAjuste);

        lblEstado = new Label();
        lblEstado.setStyle("-fx-font-size: 12px;");

        // ── Eventos ──
        btnNuevo.setOnAction(e -> abrirDialogoIngrediente(null));

        btnEditar.setOnAction(e -> {
            Map<String, Object> sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarMensaje("Selecciona un ingrediente primero", true); return; }
            abrirDialogoIngrediente(sel);
        });

        btnEliminar.setOnAction(e -> {
            Map<String, Object> sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarMensaje("Selecciona un ingrediente primero", true); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + sel.get("nombre") + "\"?\nSi tiene movimientos de stock asociados, no se podrá eliminar.",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        ApiClient.obtenerInstancia().delete("/api/ingredientes/" + ((Number) sel.get("id")).intValue());
                        mostrarMensaje("Ingrediente eliminado correctamente", false);
                        cargarDatos();
                    } catch (Exception ex) {
                        mostrarMensaje("Error: " + ex.getMessage(), true);
                    }
                }
            });
        });

        btnAjustar.setOnAction(e -> {
            Map<String, Object> sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarMensaje("Selecciona un ingrediente de la tabla", true); return; }
            if (cmbTipo.getValue() == null) { mostrarMensaje("Selecciona el tipo (ENTRADA o SALIDA)", true); return; }
            if (txtCantidad.getText().trim().isEmpty()) { mostrarMensaje("Introduce una cantidad", true); return; }
            try {
                Map<String, Object> datos = new HashMap<>();
                datos.put("ingredienteId", ((Number) sel.get("id")).longValue());
                datos.put("tipo", cmbTipo.getValue());
                datos.put("cantidad", Double.parseDouble(txtCantidad.getText().trim()));
                datos.put("descripcion", txtDescripcion.getText().trim());

                ApiClient.obtenerInstancia().post("/api/ingredientes/movimiento", datos, Map.class);
                txtCantidad.clear();
                txtDescripcion.clear();
                mostrarMensaje("Stock de \"" + sel.get("nombre") + "\" ajustado correctamente", false);
                cargarDatos();
            } catch (NumberFormatException ex) {
                mostrarMensaje("Cantidad inválida — usa un número (ej: 500 o 1.5)", true);
            } catch (Exception ex) {
                mostrarMensaje("Error: " + ex.getMessage(), true);
            }
        });

        root.getChildren().addAll(titulo, tabla, botones, panelAjuste, lblEstado);
        cargarDatos();
        return root;
    }

    private void actualizarPanelAjuste(Map<String, Object> ingrediente) {
        if (ingrediente == null) {
            lblIngSeleccionado.setText("← Selecciona un ingrediente de la tabla");
            lblIngSeleccionado.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            btnAjustar.setDisable(true);
        } else {
            lblIngSeleccionado.setText("Ingrediente seleccionado: " + ingrediente.get("nombre")
                + "  |  Stock actual: " + ingrediente.get("stockActual") + " " + ingrediente.get("unidad"));
            lblIngSeleccionado.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            btnAjustar.setDisable(false);
        }
    }

    private void abrirDialogoIngrediente(Map<String, Object> ingredienteExistente) {
        boolean esEdicion = ingredienteExistente != null;

        Stage dialogo = new Stage();
        dialogo.setTitle(esEdicion ? "Editar Ingrediente" : "Nuevo Ingrediente");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setMinWidth(420);
        dialogo.setMinHeight(300);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));

        Label titulo = new Label(esEdicion ? "Editar Ingrediente" : "Nuevo Ingrediente");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        GridPane.setHgrow(grid, Priority.ALWAYS);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Tomate triturado");
        GridPane.setHgrow(txtNombre, Priority.ALWAYS);

        ComboBox<String> cmbUnidad = new ComboBox<>();
        cmbUnidad.getItems().addAll("GRAMOS", "MILILITROS", "UNIDADES");
        cmbUnidad.setPromptText("Selecciona unidad");
        cmbUnidad.setMaxWidth(Double.MAX_VALUE);

        TextField txtStock = new TextField();
        txtStock.setPromptText("Ej: 1000");

        TextField txtMinimo = new TextField();
        txtMinimo.setPromptText("Ej: 200");

        // Pre-rellenar si es edición
        if (esEdicion) {
            txtNombre.setText(String.valueOf(ingredienteExistente.get("nombre")));
            String unidad = String.valueOf(ingredienteExistente.get("unidad"));
            cmbUnidad.setValue(unidad);
            txtStock.setText(String.valueOf(ingredienteExistente.get("stockActual")));
            txtMinimo.setText(String.valueOf(ingredienteExistente.get("stockMinimo")));
        }

        grid.add(new Label("Nombre *"),      0, 0); grid.add(txtNombre,  1, 0);
        grid.add(new Label("Unidad *"),      0, 1); grid.add(cmbUnidad,  1, 1);
        grid.add(new Label("Stock actual"),  0, 2); grid.add(txtStock,   1, 2);
        grid.add(new Label("Stock mínimo"),  0, 3); grid.add(txtMinimo,  1, 3);

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        HBox botonesInf = new HBox(10);
        botonesInf.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-cursor: hand; -fx-padding: 7 16;");
        btnCancelar.setOnAction(e -> dialogo.close());

        Button btnGuardar = new Button(esEdicion ? "Guardar cambios" : "Guardar");
        btnGuardar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 7 16;");
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) { lblErr.setText("El nombre es obligatorio"); return; }
            if (cmbUnidad.getValue() == null) { lblErr.setText("Selecciona una unidad"); return; }
            try {
                double stock  = txtStock.getText().trim().isEmpty()  ? 0 : Double.parseDouble(txtStock.getText().trim());
                double minimo = txtMinimo.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtMinimo.getText().trim());

                Map<String, Object> datos = new HashMap<>();
                datos.put("nombre",      nombre);
                datos.put("unidad",      cmbUnidad.getValue());
                datos.put("stockActual", stock);
                datos.put("stockMinimo", minimo);

                if (esEdicion) {
                    int id = ((Number) ingredienteExistente.get("id")).intValue();
                    ApiClient.obtenerInstancia().put("/api/ingredientes/" + id, datos);
                    mostrarMensaje("Ingrediente actualizado correctamente", false);
                } else {
                    ApiClient.obtenerInstancia().post("/api/ingredientes", datos, Map.class);
                    mostrarMensaje("Ingrediente \"" + nombre + "\" creado correctamente", false);
                }
                cargarDatos();
                dialogo.close();
            } catch (NumberFormatException ex) {
                lblErr.setText("Los valores de stock deben ser números (ej: 1000 o 1.5)");
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        botonesInf.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(titulo, grid, lblErr, botonesInf);
        VBox.setVgrow(grid, Priority.ALWAYS);

        dialogo.setScene(new Scene(root));
        dialogo.showAndWait();
    }

    private void cargarDatos() {
        try {
            List<Map<String, Object>> datos = ApiClient.obtenerInstancia().getList("/api/ingredientes");
            tabla.setItems(FXCollections.observableArrayList(datos));
        } catch (Exception e) {
            mostrarMensaje("Error al cargar ingredientes: " + e.getMessage(), true);
        }
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
            (esError ? "#e74c3c" : "#27ae60") + ";");
    }
}
