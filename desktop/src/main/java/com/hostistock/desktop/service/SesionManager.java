package com.hostistock.desktop.service;

public class SesionManager {

    private static SesionManager instancia;

    private String token;
    private Long barId;
    private String nombreBar;

    private SesionManager() {}

    public static synchronized SesionManager obtenerInstancia() {
        if (instancia == null) {
            instancia = new SesionManager();
        }
        return instancia;
    }

    public void iniciarSesion(String token, Long barId, String nombreBar) {
        this.token = token;
        this.barId = barId;
        this.nombreBar = nombreBar;
    }

    public void cerrarSesion() {
        this.token = null;
        this.barId = null;
        this.nombreBar = null;
    }

    public boolean estaAutenticado() {
        return token != null;
    }

    public String getToken() { return token; }
    public Long getBarId() { return barId; }
    public String getNombreBar() { return nombreBar; }
}
