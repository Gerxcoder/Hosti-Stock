package com.hostistock.desktop.controller;

import com.hostistock.desktop.HostiStockApp;
import com.hostistock.desktop.service.ApiClient;
import com.hostistock.desktop.service.SesionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Map;

public class LoginController {

    private final HostiStockApp app;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private TextField txtNombre;
    private Label lblError;
    private VBox panelRegistro;
    private boolean modoRegistro = false;

    public LoginController(HostiStockApp app) {
        this.app = app;
    }

    public VBox crearVista() {
        // ── Contenedor del formulario (tarjeta central) ──
        VBox tarjeta = new VBox(20);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(50, 40, 50, 40));
        tarjeta.setMaxWidth(500);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 4);");

        // Título
        Label titulo = new Label("Hosti-Stock");
        titulo.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Gestión de Inventario para Hostelería");
        subtitulo.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Campo nombre (solo visible en modo registro, encima de email)
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del bar");
        txtNombre.setMaxWidth(Double.MAX_VALUE);
        txtNombre.setStyle("-fx-font-size: 15px; -fx-padding: 10;");
        panelRegistro = new VBox(txtNombre);
        panelRegistro.setAlignment(Pos.CENTER);
        panelRegistro.setVisible(false);
        panelRegistro.setManaged(false);

        // Campos de login
        txtEmail = new TextField();
        txtEmail.setPromptText("Nombre de usuario o correo electrónico");
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        txtEmail.setStyle("-fx-font-size: 15px; -fx-padding: 10;");
        txtEmail.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) txtPassword.requestFocus(); });

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setMaxWidth(Double.MAX_VALUE);
        txtPassword.setStyle("-fx-font-size: 15px; -fx-padding: 10;");
        txtPassword.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) ejecutarAccion(); });

        // Botones
        Button btnAccion = new Button("Iniciar sesión");
        btnAccion.setMaxWidth(Double.MAX_VALUE);
        btnAccion.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
            + "-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 12; -fx-background-radius: 6;");
        btnAccion.setOnAction(e -> ejecutarAccion());

        Hyperlink linkAlternar = new Hyperlink("¿No tienes cuenta? Regístrate");
        linkAlternar.setStyle("-fx-font-size: 14px;");
        linkAlternar.setOnAction(e -> {
            modoRegistro = !modoRegistro;
            panelRegistro.setVisible(modoRegistro);
            panelRegistro.setManaged(modoRegistro);
            btnAccion.setText(modoRegistro ? "Registrarse" : "Iniciar Sesión");
            linkAlternar.setText(modoRegistro
                ? "¿Ya tienes cuenta? Inicia sesión"
                : "¿No tienes cuenta? Regístrate");
            lblError.setText("");
        });

        // Etiqueta de error
        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(Double.MAX_VALUE);

        Separator sep = new Separator();
        sep.setMaxWidth(Double.MAX_VALUE);

        tarjeta.getChildren().addAll(
            titulo, subtitulo,
            sep,
            panelRegistro,
            txtEmail, txtPassword,
            btnAccion, linkAlternar, lblError
        );

        // ── Root: fondo que centra la tarjeta y crece con la ventana ──
        StackPane root = new StackPane(tarjeta);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ecf0f1;");

        // La tarjeta se adapta: mínimo 400, máximo 500, con margen proporcional
        VBox wrapper = new VBox(root);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(root, Priority.ALWAYS);
        wrapper.setStyle("-fx-background-color: #ecf0f1;");

        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private void ejecutarAccion() {
        lblError.setText("");
        ApiClient api = ApiClient.obtenerInstancia();

        try {
            Map<String, Object> respuesta;

            if (modoRegistro) {
                // Validar nombre
                if (txtNombre.getText().isBlank()) {
                    lblError.setText("El nombre del bar es obligatorio");
                    return;
                }
                respuesta = api.post("/api/auth/registro",
                    Map.of(
                        "nombre", txtNombre.getText().trim(),
                        "email", txtEmail.getText().trim(),
                        "password", txtPassword.getText()
                    ), Map.class);
            } else {
                respuesta = api.post("/api/auth/login",
                    Map.of(
                        "email", txtEmail.getText().trim(),
                        "password", txtPassword.getText()
                    ), Map.class);
            }

            // Guardar sesión
            String token = (String) respuesta.get("token");
            Long barId = ((Number) respuesta.get("barId")).longValue();
            String nombreBar = (String) respuesta.get("nombreBar");

            SesionManager.obtenerInstancia().iniciarSesion(token, barId, nombreBar);

            // Navegar a la pantalla principal
            app.mostrarPrincipal();

        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }
}
