package com.expresofast.controller;

import com.expresofast.service.AuthService;
import com.expresofast.dto.*;
import com.expresofast.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class AuthControllerTest {
  @Autowired
  MockMvc mvc;
  @MockBean
  AuthService service;
  @MockBean
  JwtTokenProvider jwt;
  @MockBean
  UserDetailsService users;

  @Test
  void login_CredencialesCorrectas_RetornaToken200() throws Exception {
    when(service.login(any())).thenReturn(new AuthResponseDTO("jwt-prueba", "admin", List.of("ROLE_ADMIN"), 86400000L));
    mvc.perform(post("/api/auth/login").contentType("application/json")
        .content("{\"username\":\"admin\",\"password\":\"test\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-prueba"))
        .andExpect(jsonPath("$.username").value("admin")).andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
  }

  @Test
  void login_CredencialesIncorrectas_Retorna401() throws Exception {
    when(service.login(any())).thenThrow(new BadCredentialsException("Invalid"));
    mvc.perform(post("/api/auth/login").contentType("application/json")
        .content("{\"username\":\"admin\",\"password\":\"mal\"}"))
        .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
  }
}
