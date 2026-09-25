package com.expresofast.controller;

import com.expresofast.model.Usuario;
import com.expresofast.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "adminlab10", roles = "ADMIN")
class EnvioLab10IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;

    @Test
    void registraConsultaRastreaYActualizaConPersistencia() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("adminlab10");
        usuario.setPasswordHash("clave-prueba");
        usuario.setNombreCompleto("Administrador de prueba");
        usuario.setEmail("lab10@example.com");
        usuario.setActivo(true);
        usuarios.save(usuario);
        String body = mvc.perform(post("/api/v1/envios").contentType("application/json")
                .content("""
                    {"destinatario":"Ana", "direccionDestino":"Cartago", "montoFlete":2500}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaCreacion").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var envio = mapper.readTree(body);
        String codigo = envio.get("codigoRastreo").asText();
        int id = envio.get("id").asInt();
        assertTrue(codigo.matches("EXP-\\d{4}-[A-F0-9]{16}"));
        mvc.perform(get("/api/v1/envios")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoRastreo").value(codigo));
        mvc.perform(get("/api/v1/envios/rastreo/" + codigo.toLowerCase()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.destinatario").value("Ana"));
        mvc.perform(patch("/api/v1/envios/" + id + "/estado").contentType("application/json")
                .content("{\"nuevoEstado\":\"EN_TRANSITO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_TRANSITO"));
        mvc.perform(get("/api/v1/envios/rastreo/" + codigo))
                .andExpect(jsonPath("$.estado").value("EN_TRANSITO"));
        mvc.perform(patch("/api/v1/envios/" + id + "/estado").contentType("application/json")
                .content("{\"nuevoEstado\":\"PENDIENTE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validaCamposYGuiaInexistente() throws Exception {
        mvc.perform(post("/api/v1/envios").contentType("application/json")
                .content("{\"destinatario\":\" \",\"direccionDestino\":\"\",\"montoFlete\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/envios/rastreo/NO-EXISTE")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CONDUCTOR")
    void conductorNoPuedeCrear() throws Exception {
        mvc.perform(post("/api/v1/envios").contentType("application/json")
                .content("{\"destinatario\":\"Ana\",\"direccionDestino\":\"Cartago\",\"montoFlete\":1000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void permitePreflightDeAngular() throws Exception {
        mvc.perform(options("/api/v1/envios").header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
