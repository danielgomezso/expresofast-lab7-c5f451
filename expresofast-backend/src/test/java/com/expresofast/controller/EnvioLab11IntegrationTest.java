package com.expresofast.controller;

import com.expresofast.repository.EnvioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class EnvioLab11IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EnvioRepository envios;
    @Autowired EntityManager em;

    private String registro(String tracking) {
        return """
                {"numeroTracking":"%s","destinatario":"Ana","direccionDestino":"Cartago",
                 "montoFlete":2500,"fechaDespacho":"2026-10-02","fechaEntregaEstimada":"2026-10-03",
                 "paquetes":[{"descripcion":"Libros","pesoKg":1.25},{"descripcion":"Ropa","pesoKg":2.50}]}
                """.formatted(tracking);
    }

    @Test
    void guardaPaquetesFechasYPesoYDetectaDuplicados() throws Exception {
        mvc.perform(get("/api/envios/check-tracking/EXP-LAB11"))
                .andExpect(status().isOk()).andExpect(content().string("false"));
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json").content(registro("exp-lab11")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigoRastreo").value("EXP-LAB11"));
        em.flush();
        em.clear();
        var envio = envios.findByCodigoRastreo("EXP-LAB11").orElseThrow();
        assertEquals(2, envio.getPaquetes().size());
        assertEquals(0, new BigDecimal("3.75").compareTo(envio.getPesoKg()));
        assertEquals("2026-10-02", envio.getFechaDespacho().toString());
        assertEquals("2026-10-03", envio.getFechaEntregaEstimada().toString());
        assertEquals(envio.getId(), envio.getPaquetes().getFirst().getEnvio().getId());
        mvc.perform(get("/api/v1/envios/check-tracking/exp-lab11"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json").content(registro("EXP-LAB11")))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaFechasIgualesYPesoInvalidoSinGuardar() throws Exception {
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json")
                .content(registro("EXP-FECHAS").replace("2026-10-03", "2026-10-02")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json")
                .content(registro("EXP-PESO").replace("2.50", "-1")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json")
                .content(registro("EXP-VACIO").replace("[{\"descripcion\":\"Libros\",\"pesoKg\":1.25},{\"descripcion\":\"Ropa\",\"pesoKg\":2.50}]", "[]")))
                .andExpect(status().isBadRequest());
        assertFalse(envios.existsByCodigoRastreoIgnoreCase("EXP-FECHAS"));
        assertFalse(envios.existsByCodigoRastreoIgnoreCase("EXP-PESO"));
        assertFalse(envios.existsByCodigoRastreoIgnoreCase("EXP-VACIO"));
    }

    @Test
    @WithMockUser(roles = "CONDUCTOR")
    void conductorNoPuedeRegistrar() throws Exception {
        mvc.perform(post("/api/v1/envios/registro").contentType("application/json").content(registro("EXP-PERMISO")))
                .andExpect(status().isForbidden());
    }
}
