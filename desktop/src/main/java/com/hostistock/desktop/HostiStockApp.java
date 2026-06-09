package com.hostistock.desktop;

import com.hostistock.desktop.controller.*;
import com.hostistock.desktop.service.SesionManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HostiStockApp extends Application {

    private Stage ventanaPrincipal;
    private BorderPane layoutPrincipal;
    private StackPane contenido;

    @Override
    public void start(Stage stage) {
        this.ventanaPrincipal = stage;
        stage.setTitle("Hosti-Stock");
        stage.setWidth(1100);
        stage.setHeight(750);

        mostrarLogin();
        stage.show();
    }

    public void mostrarLogin() {
        LoginController loginCtrl = new LoginController(this);
        Scene scene = new Scene(loginCtrl.crearVista());
        ventanaPrincipal.setScene(scene);
    }

    public void mostrarPrincipal() {
        layoutPrincipal = new BorderPane();
        contenido = new StackPane();
        contenido.setPadding(new Insets(15));

        // ── Barra lateral de navegación ──
        VBox navegacion = crearNavegacion();

        layoutPrincipal.setLeft(navegacion);
        layoutPrincipal.setCenter(contenido);

        Scene scene = new Scene(layoutPrincipal, 1100, 750);
        ventanaPrincipal.setScene(scene);

        // Mostrar dashboard por defecto
        cargarDashboard();
    }

    private VBox crearNavegacion() {
        VBox nav = new VBox(5);
        nav.setPadding(new Insets(10));
        nav.setStyle("-fx-background-color: #2c3e50;");
        nav.setPrefWidth(200);
        nav.setAlignment(Pos.TOP_CENTER);

        // Título
        Label titulo = new Label("Hosti-Stock");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label barNombre = new Label(SesionManager.obtenerInstancia().getNombreBar());
        barNombre.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #34495e;");

        // Botones de navegación
        Button btnDashboard = crearBotonNav("Dashboard");
        Button btnPlatos = crearBotonNav("Platos");
        Button btnIngredientes = crearBotonNav("Ingredientes");
        Button btnConsumos = crearBotonNav("Consumos");
        Button btnPredicciones = crearBotonNav("Predicciones IA");

        Separator sep2 = new Separator();
        Button btnCerrarSesion = new Button("Cerrar Sesión");
        btnCerrarSesion.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
            + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;");
        btnCerrarSesion.setOnMouseEntered(e ->
            btnCerrarSesion.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;"));
        btnCerrarSesion.setOnMouseExited(e ->
            btnCerrarSesion.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;"));

        // Eventos
        btnDashboard.setOnAction(e -> cargarDashboard());
        btnIngredientes.setOnAction(e -> cargarIngredientes());
        btnPlatos.setOnAction(e -> cargarPlatos());
        btnConsumos.setOnAction(e -> cargarConsumos());
        btnPredicciones.setOnAction(e -> cargarPredicciones());
        btnCerrarSesion.setOnAction(e -> {
            SesionManager.obtenerInstancia().cerrarSesion();
            mostrarLogin();
        });

        nav.getChildren().addAll(
            titulo, barNombre, sep,
            btnDashboard, btnPlatos, btnIngredientes, btnConsumos, btnPredicciones,
            sep2, btnCerrarSesion
        );

        VBox.setMargin(titulo, new Insets(10, 0, 0, 0));
        VBox.setMargin(barNombre, new Insets(0, 0, 10, 0));
        VBox.setMargin(sep2, new Insets(20, 0, 0, 0));

        return nav;
    }

    private Button crearBotonNav(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; "
            + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;");
        btn.setOnMouseEntered(e ->
            btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;"));
        btn.setOnMouseExited(e ->
            btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-pref-width: 180; -fx-font-size: 13px;"));
        return btn;
    }

    private void cargarDashboard() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new DashboardController().crearVista());
    }

    private void cargarIngredientes() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new IngredientesController().crearVista());
    }

    private void cargarPlatos() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new PlatosController().crearVista());
    }

    private void cargarConsumos() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new ConsumosController().crearVista());
    }

    private void cargarPredicciones() {
        contenido.getChildren().clear();
        contenido.getChildren().add(new PrediccionesController().crearVista());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
