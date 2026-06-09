package com.hostistock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostistock.dto.IngredienteRequest;
import com.hostistock.dto.LoginResponse;
import com.hostistock.dto.RegistroBarRequest;
import com.hostistock.model.TipoUnidad;
import com.hostistock.service.AutenticacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IngredienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AutenticacionService autenticacionService;

    private String token;

    @BeforeEach
    void setUp() {
        LoginResponse loginResponse = autenticacionService.registrar(
            new RegistroBarRequest("Bar Test", "test@ingredientes.com", "clave123"));
        this.token = loginResponse.token();
    }

    @Test
    void crearYListar_debeCrearIngredienteYDevolverEnLista() throws Exception {
        IngredienteRequest request = new IngredienteRequest(
            "Tomate", TipoUnidad.GRAMOS, new BigDecimal("5000"), new BigDecimal("1000"));

        mockMvc.perform(post("/api/ingredientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Tomate"))
            .andExpect(jsonPath("$.unidad").value("GRAMOS"))
            .andExpect(jsonPath("$.stockActual").value(5000));

        mockMvc.perform(get("/api/ingredientes")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Tomate"));
    }

    @Test
    void obtenerPorId_conIdExistente_debeDevolver200() throws Exception {
        IngredienteRequest request = new IngredienteRequest(
            "Aceite", TipoUnidad.MILILITROS, new BigDecimal("3000"), new BigDecimal("500"));

        String responseJson = mockMvc.perform(post("/api/ingredientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/ingredientes/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Aceite"));
    }

    @Test
    void actualizar_conDatosValidos_debeModificarIngrediente() throws Exception {
        IngredienteRequest createReq = new IngredienteRequest(
            "Patata", TipoUnidad.GRAMOS, new BigDecimal("2000"), new BigDecimal("500"));

        String responseJson = mockMvc.perform(post("/api/ingredientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(responseJson).get("id").asLong();

        IngredienteRequest updateReq = new IngredienteRequest(
            "Patata gallega", TipoUnidad.GRAMOS, new BigDecimal("3000"), new BigDecimal("800"));

        mockMvc.perform(put("/api/ingredientes/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Patata gallega"))
            .andExpect(jsonPath("$.stockActual").value(3000));
    }

    @Test
    void eliminar_conIdExistente_debeDevolver204() throws Exception {
        IngredienteRequest request = new IngredienteRequest(
            "Cebolla", TipoUnidad.GRAMOS, new BigDecimal("1000"), new BigDecimal("200"));

        String responseJson = mockMvc.perform(post("/api/ingredientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(delete("/api/ingredientes/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ingredientes/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void listar_sinToken_debeDevolver403() throws Exception {
        mockMvc.perform(get("/api/ingredientes"))
            .andExpect(status().isForbidden());
    }
}