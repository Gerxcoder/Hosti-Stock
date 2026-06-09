package com.hostistock.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL;
    private static ApiClient instancia;

    static {
        String url = System.getProperty("hostistock.api.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("HOSTISTOCK_API_URL");
        }
        if (url == null || url.isBlank()) {
            url = "http://localhost:8080";
        }
        BASE_URL = url;
    }

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized ApiClient obtenerInstancia() {
        if (instancia == null) {
            instancia = new ApiClient();
        }
        return instancia;
    }

    public <T> T get(String ruta, Class<T> tipo) throws Exception {
        HttpRequest request = construirGetRequest(ruta);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), tipo);
    }

    public List<Map<String, Object>> getList(String ruta) throws Exception {
        HttpRequest request = construirGetRequest(ruta);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public String getString(String ruta) throws Exception {
        HttpRequest request = construirGetRequest(ruta);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return response.body();
    }

    public <T> T post(String ruta, Object cuerpo, Class<T> tipo) throws Exception {
        String json = objectMapper.writeValueAsString(cuerpo);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));

        agregarAutorizacion(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), tipo);
    }

    public List<Map<String, Object>> postList(String ruta, Object cuerpo) throws Exception {
        String json = objectMapper.writeValueAsString(cuerpo);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));

        agregarAutorizacion(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> postSinCuerpo(String ruta) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.noBody());

        agregarAutorizacion(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> put(String ruta, Object cuerpo) throws Exception {
        String json = objectMapper.writeValueAsString(cuerpo);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(json));

        agregarAutorizacion(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        verificarRespuesta(response);
        return objectMapper.readValue(response.body(), Map.class);
    }

    public void delete(String ruta) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .DELETE();

        agregarAutorizacion(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Error " + response.statusCode() + ": " + response.body());
        }
    }

    private HttpRequest construirGetRequest(String ruta) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + ruta))
            .GET();

        agregarAutorizacion(builder);
        return builder.build();
    }

    private void agregarAutorizacion(HttpRequest.Builder builder) {
        SesionManager sesion = SesionManager.obtenerInstancia();
        if (sesion.estaAutenticado()) {
            builder.header("Authorization", "Bearer " + sesion.getToken());
        }
    }

    private void verificarRespuesta(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            // Intentar extraer mensaje de error del JSON
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = objectMapper.readValue(response.body(), Map.class);
                String mensaje = (String) error.getOrDefault("mensaje", response.body());
                throw new RuntimeException(mensaje);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error " + response.statusCode() + ": " + response.body());
            }
        }
    }
}
