package com.hostistock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostistock.dto.LoginRequest;
import com.hostistock.dto.RegistroBarRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AutenticacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registro_conDatosValidos_debeCrearBarYDevolverToken() throws Exception {
        RegistroBarRequest request = new RegistroBarRequest(
            "Bar La Esquina", "laesquina@test.com", "clave123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.barId").isNumber())
            .andExpect(jsonPath("$.nombreBar").value("Bar La Esquina"));
    }

    @Test
    void registro_conEmailDuplicado_debeDevolver400() throws Exception {
        RegistroBarRequest request = new RegistroBarRequest(
            "Bar Uno", "duplicado@test.com", "clave123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registro_sinNombre_debeDevolver400() throws Exception {
        RegistroBarRequest request = new RegistroBarRequest(
            "", "test@test.com", "clave123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_conCredencialesCorrectas_debeDevolver200ConToken() throws Exception {
        RegistroBarRequest registro = new RegistroBarRequest(
            "Bar Login Test", "login@test.com", "clave123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("login@test.com", "clave123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.nombreBar").value("Bar Login Test"));
    }

    @Test
    void login_conPasswordIncorrecto_debeDevolver401() throws Exception {
        RegistroBarRequest registro = new RegistroBarRequest(
            "Bar Fail", "fail@test.com", "correcta123");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("fail@test.com", "incorrecta");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized());
    }
}