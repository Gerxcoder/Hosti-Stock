package com.hostistock.desktop;

// Workaround para fat JAR: si main() extiende Application directamente, JavaFX falla al empaquetar
public class Launcher {

    public static void main(String[] args) {
        HostiStockApp.main(args);
    }
}
