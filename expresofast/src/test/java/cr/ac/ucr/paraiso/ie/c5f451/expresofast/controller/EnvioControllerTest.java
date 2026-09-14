package cr.ac.ucr.paraiso.ie.c5f451.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(EnvioController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@WithMockUser(roles = "ADMIN")
class EnvioControllerTest {
  @Autowired
  MockMvc mvc;
  @MockBean
  EnvioService service;
  @MockBean
  JwtTokenProvider jwt;
  @MockBean
  UserDetailsService users;
  static final String VALID = """
      {"codigoRastreo":"EXP-1234","direccionDestino":"Paraíso","pesoKg":5,"costo":2500,"vehiculo":{"id":1},"conductor":{"id":2}}
      """;

  @Test
  void obtenerEnvio_Existente_Retorna200() throws Exception {
    EnvioResponseDTO dto = new EnvioResponseDTO();
    dto.setId(1);
    dto.setCodigoRastreo("EXP-1234");
    dto.setEstadoEnvio("PENDIENTE");
    when(service.obtenerEnvioPorId(1)).thenReturn(dto);
    mvc.perform(get("/api/envios/1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"))
        .andExpect(jsonPath("$.estadoEnvio").value("PENDIENTE"));
  }

  @Test
  void obtenerEnvio_Inexistente_Retorna404RFC7807() throws Exception {
    when(service.obtenerEnvioPorId(99)).thenThrow(new ResourceNotFoundException("El envío no existe."));
    mvc.perform(get("/api/envios/99")).andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.type").value("about:blank")).andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("El envío no existe."))
        .andExpect(jsonPath("$.instance").value("/api/envios/99"));
  }

  @Test
  void registrarEnvio_CamposNulos_Retorna400() throws Exception {
    mvc.perform(post("/api/envios").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.codigoRastreo").exists())
        .andExpect(jsonPath("$.errors.pesoKg").exists())
        .andExpect(jsonPath("$.errors.direccionDestino").exists()).andExpect(jsonPath("$.errors.costo").exists());
    verifyNoInteractions(service);
  }

  @SuppressWarnings("null")
  @Test
  void registrarEnvio_CamposVaciosYPesoNegativo_Retorna400() throws Exception {
    mvc.perform(post("/api/envios").contentType("application/json")
        .content(VALID.replace("EXP-1234", " ").replace("\"pesoKg\":5", "\"pesoKg\":-1")))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.codigoRastreo").exists())
        .andExpect(jsonPath("$.errors.pesoKg").exists());
    verifyNoInteractions(service);
  }

  @Test
  void registrarEnvio_Valido_Retorna201() throws Exception {
    Envio e = new Envio();
    e.setId(3);
    e.setCodigoRastreo("EXP-1234");
    e.setEstadoEnvio("PENDIENTE");
    when(service.registrarEnvio(any())).thenReturn(e);
    mvc.perform(post("/api/envios").contentType("application/json").content(VALID)).andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(3)).andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    verify(service).registrarEnvio(any());
  }

  @Test
  @WithMockUser(roles = "CONDUCTOR")
  void registrarEnvio_Conductor_Retorna403() throws Exception {
    mvc.perform(post("/api/envios").contentType("application/json").content(VALID)).andExpect(status().isForbidden());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "OPERADOR")
  void registrarEnvio_Operador_Retorna201() throws Exception {
    when(service.registrarEnvio(any())).thenReturn(new Envio());
    mvc.perform(post("/api/envios").contentType("application/json").content(VALID)).andExpect(status().isCreated());
  }

  @Test
  void jsonMalformado_Retorna400() throws Exception {
    mvc.perform(post("/api/envios").contentType("application/json").content("{")).andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }
}
