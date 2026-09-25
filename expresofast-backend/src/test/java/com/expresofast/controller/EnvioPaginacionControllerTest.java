package com.expresofast.controller;

import com.expresofast.service.EnvioService;
import com.expresofast.dto.EnvioDTO;
import com.expresofast.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@WithMockUser(roles = "OPERADOR")
class EnvioPaginacionControllerTest {
    @Autowired MockMvc mvc;
    @MockBean EnvioService service;
    @MockBean JwtTokenProvider jwt;
    @MockBean UserDetailsService users;

    @Test
    void consultaConValoresPorDefecto() throws Exception {
        EnvioDTO dto = new EnvioDTO(1, "EXP-9001", "Ana", "Cartago", new BigDecimal("2500"),
                "PENDIENTE", LocalDateTime.of(2026, 9, 1, 10, 0));
        when(service.listarPaginado(0, 5, "fechaCreacion", "DESC", "", ""))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 16));
        mvc.perform(get("/api/v1/envios/paginados")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].destinatario").value("Ana"))
                .andExpect(jsonPath("$.content[0].montoFlete").value(2500))
                .andExpect(jsonPath("$.content[0].vehiculo").doesNotExist())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(16))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void pasaLosFiltrosAlServicio() throws Exception {
        when(service.listarPaginado(1, 10, "montoFlete", "ASC", "Ana", "PENDIENTE"))
                .thenReturn(Page.empty(PageRequest.of(1, 10)));
        mvc.perform(get("/api/v1/envios/paginados").param("page", "1").param("size", "10")
                .param("sortBy", "montoFlete").param("direction", "ASC")
                .param("busqueda", "Ana").param("estado", "PENDIENTE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        verify(service).listarPaginado(1, 10, "montoFlete", "ASC", "Ana", "PENDIENTE");
    }

    @Test
    void parametroInvalidoRetorna400() throws Exception {
        when(service.listarPaginado(-1, 5, "fechaCreacion", "DESC", "", ""))
                .thenThrow(new IllegalArgumentException("Página inválida"));
        mvc.perform(get("/api/v1/envios/paginados").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Página inválida"));
        mvc.perform(get("/api/v1/envios/paginados").param("page", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void consultaProcedimiento() throws Exception {
        when(service.listarViaStoredProcedure("ENTREGADO")).thenReturn(List.of());
        mvc.perform(get("/api/v1/envios/procedimiento/ENTREGADO"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).listarViaStoredProcedure("ENTREGADO");
    }

    @Test
    @WithMockUser(roles = "INVITADO")
    void rechazaRolSinPermisos() throws Exception {
        mvc.perform(get("/api/v1/envios/paginados")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/envios/procedimiento/PENDIENTE")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
