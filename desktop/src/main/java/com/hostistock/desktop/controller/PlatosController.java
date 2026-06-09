package com.hostistock.desktop.controller;

import com.hostistock.desktop.service.ApiClient;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.*;

public class PlatosController {

    private TableView<Map<String, Object>> tablaPlatos;
    private TableView<Map<String, Object>> tablaReceta;
    private Label lblRecetaTitulo;
    private Label lblEstado;

    @SuppressWarnings("unchecked")
    public VBox crearVista() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titulo = new Label("Gestión de Platos y Recetas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Tabla de platos ──
        tablaPlatos = new TableView<>();
        tablaPlatos.setPrefHeight(270);
        tablaPlatos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaPlatos.setPlaceholder(new Label("No hay platos creados todavía"));

        TableColumn<Map<String, Object>, Object> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("id")));
        colId.setMaxWidth(55);
        colId.setMinWidth(55);

        TableColumn<Map<String, Object>, Object> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombre")));

        TableColumn<Map<String, Object>, Object> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("categoria")));
        colCategoria.setPrefWidth(130);

        TableColumn<Map<String, Object>, Object> colDispo = new TableColumn<>("Disponibles");
        colDispo.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("unidadesDisponibles")));
        colDispo.setPrefWidth(100);

        tablaPlatos.getColumns().addAll(colId, colNombre, colCategoria, colDispo);
        tablaPlatos.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nuevo) -> mostrarReceta(nuevo));

        // ── Botones principales ──
        HBox botones = new HBox(10);
        botones.setPadding(new Insets(4, 0, 4, 0));

        Button btnNuevo = new Button("+ Nuevo Plato");
        btnNuevo.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");

        Button btnEliminar = new Button("Eliminar seleccionado");
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");

        botones.getChildren().addAll(btnNuevo, btnEliminar);

        // ── Receta del plato seleccionado ──
        lblRecetaTitulo = new Label("Selecciona un plato para ver su receta");
        lblRecetaTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        tablaReceta = new TableView<>();
        tablaReceta.setPrefHeight(160);
        tablaReceta.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaReceta.setPlaceholder(new Label("Este plato no tiene receta definida"));

        TableColumn<Map<String, Object>, Object> colRIng = new TableColumn<>("Ingrediente");
        colRIng.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombreIngrediente")));

        TableColumn<Map<String, Object>, Object> colRCant = new TableColumn<>("Cantidad");
        colRCant.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("cantidad")));
        colRCant.setPrefWidth(100);

        TableColumn<Map<String, Object>, Object> colRUnidad = new TableColumn<>("Unidad");
        colRUnidad.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("unidad")));
        colRUnidad.setPrefWidth(100);

        tablaReceta.getColumns().addAll(colRIng, colRCant, colRUnidad);

        lblEstado = new Label();
        lblEstado.setStyle("-fx-font-size: 12px;");

        // ── Eventos ──
        btnNuevo.setOnAction(e -> abrirDialogoCrearPlato());

        btnEliminar.setOnAction(e -> {
            Map<String, Object> sel = tablaPlatos.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarMensaje("Selecciona un plato primero", true); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el plato \"" + sel.get("nombre") + "\"?",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        ApiClient.obtenerInstancia().delete("/api/platos/" + ((Number) sel.get("id")).intValue());
                        mostrarMensaje("Plato eliminado correctamente", false);
                        tablaReceta.getItems().clear();
                        lblRecetaTitulo.setText("Selecciona un plato para ver su receta");
                        cargarDatos();
                    } catch (Exception ex) {
                        mostrarMensaje("Error: " + ex.getMessage(), true);
                    }
                }
            });
        });

        root.getChildren().addAll(titulo, tablaPlatos, botones, lblRecetaTitulo, tablaReceta, lblEstado);
        cargarDatos();
        return root;
    }

    @SuppressWarnings("unchecked")
    private void abrirDialogoCrearPlato() {
        Stage dialogo = new Stage();
        dialogo.setTitle("Crear Nuevo Plato");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setMinWidth(760);
        dialogo.setMinHeight(540);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        // ── Nombre + Categoría ──
        HBox campos = new HBox(12);
        campos.setAlignment(Pos.CENTER_LEFT);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del plato *");
        HBox.setHgrow(txtNombre, Priority.ALWAYS);

        TextField txtCategoria = new TextField();
        txtCategoria.setPromptText("Categoría (opcional)");
        txtCategoria.setPrefWidth(180);

        campos.getChildren().addAll(
            new Label("Nombre:"), txtNombre,
            new Label("  Categoría:"), txtCategoria
        );

        // ── Panel central: ingredientes disponibles | receta ──
        HBox paneles = new HBox(15);
        VBox.setVgrow(paneles, Priority.ALWAYS);

        // Panel izquierdo: lista de ingredientes
        VBox panelIzq = new VBox(8);
        HBox.setHgrow(panelIzq, Priority.ALWAYS);

        Label lblIzq = new Label("Ingredientes disponibles");
        lblIzq.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        TableView<Map<String, Object>> tablaIng = new TableView<>();
        tablaIng.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaIng.setPlaceholder(new Label("Sin ingredientes"));
        VBox.setVgrow(tablaIng, Priority.ALWAYS);

        TableColumn<Map<String, Object>, Object> colIngNombre = new TableColumn<>("Nombre");
        colIngNombre.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombre")));

        TableColumn<Map<String, Object>, Object> colIngStock = new TableColumn<>("Stock");
        colIngStock.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("stockActual")));
        colIngStock.setPrefWidth(75);

        TableColumn<Map<String, Object>, Object> colIngUnidad = new TableColumn<>("Unidad");
        colIngUnidad.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("unidad")));
        colIngUnidad.setPrefWidth(85);

        tablaIng.getColumns().addAll(colIngNombre, colIngStock, colIngUnidad);

        try {
            List<Map<String, Object>> ings = ApiClient.obtenerInstancia().getList("/api/ingredientes");
            tablaIng.setItems(FXCollections.observableArrayList(ings));
        } catch (Exception e) {
            tablaIng.setPlaceholder(new Label("Error al cargar ingredientes"));
        }

        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");
        txtCantidad.setPrefWidth(90);
        HBox.setHgrow(txtCantidad, Priority.ALWAYS);
        Button btnAnadir = new Button("Añadir →");
        btnAnadir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12;");

        Button btnCrearIng = new Button("+ Crear ingrediente");
        btnCrearIng.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10;");
        btnCrearIng.setOnAction(e -> {
            abrirDialogoCrearIngrediente(tablaIng);
        });

        addRow.getChildren().addAll(new Label("Cantidad:"), txtCantidad, btnAnadir, btnCrearIng);

        panelIzq.getChildren().addAll(lblIzq, tablaIng, addRow);

        // Panel derecho: receta en construcción
        VBox panelDer = new VBox(8);
        HBox.setHgrow(panelDer, Priority.ALWAYS);

        Label lblDer = new Label("Receta del plato");
        lblDer.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        ObservableList<Map<String, Object>> recetaItems = FXCollections.observableArrayList();

        TableView<Map<String, Object>> tablaRecetaDlg = new TableView<>();
        tablaRecetaDlg.setItems(recetaItems);
        tablaRecetaDlg.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaRecetaDlg.setPlaceholder(new Label("Añade ingredientes desde la izquierda"));
        VBox.setVgrow(tablaRecetaDlg, Priority.ALWAYS);

        TableColumn<Map<String, Object>, Object> colRNombre = new TableColumn<>("Ingrediente");
        colRNombre.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("nombre")));

        TableColumn<Map<String, Object>, Object> colRCant = new TableColumn<>("Cantidad");
        colRCant.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("cantidad")));
        colRCant.setPrefWidth(80);

        TableColumn<Map<String, Object>, Object> colRUnid = new TableColumn<>("Unidad");
        colRUnid.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().get("unidad")));
        colRUnid.setPrefWidth(80);

        tablaRecetaDlg.getColumns().addAll(colRNombre, colRCant, colRUnid);

        Button btnQuitar = new Button("← Quitar seleccionado");
        btnQuitar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12;");
        btnQuitar.setOnAction(e -> {
            Map<String, Object> sel = tablaRecetaDlg.getSelectionModel().getSelectedItem();
            if (sel != null) recetaItems.remove(sel);
        });

        panelDer.getChildren().addAll(lblDer, tablaRecetaDlg, btnQuitar);
        paneles.getChildren().addAll(panelIzq, panelDer);

        // ── Mensaje de estado del diálogo ──
        Label lblDlgEstado = new Label();
        lblDlgEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");

        // Evento añadir ingrediente a receta
        btnAnadir.setOnAction(e -> {
            Map<String, Object> ingSel = tablaIng.getSelectionModel().getSelectedItem();
            if (ingSel == null) {
                lblDlgEstado.setText("Selecciona un ingrediente de la lista de la izquierda");
                return;
            }
            String cantStr = txtCantidad.getText().trim();
            if (cantStr.isEmpty()) {
                lblDlgEstado.setText("Introduce una cantidad");
                return;
            }
            try {
                double cantidad = Double.parseDouble(cantStr);
                if (cantidad <= 0) {
                    lblDlgEstado.setText("La cantidad debe ser mayor que 0");
                    return;
                }
                boolean yaExiste = recetaItems.stream()
                    .anyMatch(r -> r.get("id").equals(ingSel.get("id")));
                if (yaExiste) {
                    lblDlgEstado.setText("Ese ingrediente ya está en la receta");
                    return;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", ingSel.get("id"));
                item.put("nombre", ingSel.get("nombre"));
                item.put("unidad", ingSel.get("unidad"));
                item.put("cantidad", cantidad);
                recetaItems.add(item);
                txtCantidad.clear();
                lblDlgEstado.setText("");
            } catch (NumberFormatException ex) {
                lblDlgEstado.setText("Cantidad inválida — usa un número (ej: 200 o 1.5)");
            }
        });

        // ── Botones inferiores ──
        HBox botonesInf = new HBox(10);
        botonesInf.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-cursor: hand; -fx-padding: 8 18;");
        btnCancelar.setOnAction(e -> dialogo.close());

        Button btnGuardar = new Button("Guardar Plato");
        btnGuardar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 8 18;");
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                lblDlgEstado.setText("El nombre del plato es obligatorio");
                return;
            }
            try {
                List<Map<String, Object>> receta = new ArrayList<>();
                for (Map<String, Object> item : recetaItems) {
                    receta.add(Map.of(
                        "ingredienteId", ((Number) item.get("id")).longValue(),
                        "cantidad", item.get("cantidad")
                    ));
                }
                Map<String, Object> datos = new HashMap<>();
                datos.put("nombre", nombre);
                datos.put("categoria", txtCategoria.getText().trim().isEmpty()
                    ? null : txtCategoria.getText().trim());
                datos.put("receta", receta);

                ApiClient.obtenerInstancia().post("/api/platos", datos, Map.class);
                mostrarMensaje("Plato \"" + nombre + "\" creado correctamente", false);
                cargarDatos();
                dialogo.close();
            } catch (Exception ex) {
                lblDlgEstado.setText("Error: " + ex.getMessage());
            }
        });

        botonesInf.getChildren().addAll(btnCancelar, btnGuardar);

        root.getChildren().addAll(campos, paneles, lblDlgEstado, botonesInf);
        dialogo.setScene(new Scene(root));
        dialogo.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private void mostrarReceta(Map<String, Object> plato) {
        tablaReceta.getItems().clear();
        if (plato == null) {
            lblRecetaTitulo.setText("Selecciona un plato para ver su receta");
            return;
        }
        lblRecetaTitulo.setText("Receta de \"" + plato.get("nombre") + "\":");
        Object recetaObj = plato.get("receta");
        if (recetaObj instanceof List) {
            for (Object item : (List<?>) recetaObj) {
                if (item instanceof Map) {
                    tablaReceta.getItems().add((Map<String, Object>) item);
                }
            }
        }
    }

    private void abrirDialogoCrearIngrediente(TableView<Map<String, Object>> tablaIng) {
        Stage dialogo = new Stage();
        dialogo.setTitle("Nuevo Ingrediente");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setMinWidth(420);
        dialogo.setMinHeight(300);

        VBox root = new VBox(14);
        root.setPadding(new Insets(22));

        Label titulo = new Label("Nuevo Ingrediente");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

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

        grid.add(new Label("Nombre *"),      0, 0); grid.add(txtNombre,  1, 0);
        grid.add(new Label("Unidad *"),      0, 1); grid.add(cmbUnidad,  1, 1);
        grid.add(new Label("Stock inicial"), 0, 2); grid.add(txtStock,   1, 2);
        grid.add(new Label("Stock mínimo"),  0, 3); grid.add(txtMinimo,  1, 3);

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        HBox botonesInf = new HBox(10);
        botonesInf.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-cursor: hand; -fx-padding: 7 16;");
        btnCancelar.setOnAction(e -> dialogo.close());

        Button btnGuardar = new Button("Guardar");
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

                ApiClient.obtenerInstancia().post("/api/ingredientes", datos, Map.class);

                // Recargar la tabla de ingredientes del diálogo de platos
                List<Map<String, Object>> ings = ApiClient.obtenerInstancia().getList("/api/ingredientes");
                tablaIng.setItems(FXCollections.observableArrayList(ings));

                dialogo.close();
            } catch (NumberFormatException ex) {
                lblErr.setText("Los valores de stock deben ser números (ej: 1000 o 1.5)");
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        botonesInf.getChildren().addAll(btnCancelar, btnGuardar);
        root.getChildren().addAll(titulo, grid, lblErr, botonesInf);

        dialogo.setScene(new Scene(root));
        dialogo.showAndWait();
    }

    private void cargarDatos() {
        try {
            List<Map<String, Object>> datos = ApiClient.obtenerInstancia().getList("/api/platos");
            tablaPlatos.setItems(FXCollections.observableArrayList(datos));
        } catch (Exception e) {
            mostrarMensaje("Error al cargar platos: " + e.getMessage(), true);
        }
    }

    private void mostrarMensaje(String msg, boolean esError) {
        lblEstado.setText(msg);
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
            (esError ? "#e74c3c" : "#27ae60") + ";");
    }
}
