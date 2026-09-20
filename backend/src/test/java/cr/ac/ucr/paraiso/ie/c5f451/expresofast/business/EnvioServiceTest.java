package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.repository.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {
  @Mock
  EnvioRepository envios;
  @Mock
  VehiculoRepository vehiculos;
  @Mock
  BitacoraEnvioRepository bitacoras;
  @Mock
  UsuarioRepository usuarios;
  @InjectMocks
  EnvioService service;
  Envio envio;
  Vehiculo vehiculo;
  Conductor conductor;

  @BeforeEach
  void setup() {
    vehiculo = new Vehiculo();
    vehiculo.setId(1);
    vehiculo.setPlaca("ABC123");
    vehiculo.setCapacidadKg(new BigDecimal("100"));
    conductor = new Conductor();
    conductor.setId(2);
    conductor.setNombre("Ana");
    envio = new Envio();
    envio.setId(3);
    envio.setCodigoRastreo("EXP-1234");
    envio.setDireccionDestino("Paraíso");
    envio.setPesoKg(new BigDecimal("5"));
    envio.setCosto(new BigDecimal("2500"));
    envio.setEstadoEnvio("PENDIENTE");
    envio.setVehiculo(vehiculo);
    envio.setConductor(conductor);
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @SuppressWarnings("null")
  @Test
  void crearEnvio_DatosValidos_RetornaEnvioDTO() {
    envio.setEstadoEnvio("ENTREGADO");
    when(vehiculos.findById(1)).thenReturn(Optional.of(vehiculo));
    when(envios.save(envio)).thenReturn(envio);
    EnvioResponseDTO result = service.crearEnvio(envio);
    assertAll(() -> assertEquals("PENDIENTE", result.getEstadoEnvio()),
        () -> assertEquals("EXP-1234", result.getCodigoRastreo()),
        () -> assertEquals("ABC123", result.getPlacaVehiculo()), () -> assertEquals("Ana", result.getNombreConductor()),
        () -> assertEquals(new BigDecimal("2500"), result.getCosto()));
    verify(envios).save(envio);
  }

  @SuppressWarnings("null")
  @Test
  void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
    envio.setPesoKg(new BigDecimal("100.01"));
    when(vehiculos.findById(1)).thenReturn(Optional.of(vehiculo));
    assertThrows(CapacidadExcedidaException.class, () -> service.crearEnvio(envio));
    verify(envios, never()).save(any());
  }

  @SuppressWarnings("null")
  @Test
  void capacidadExacta_PermiteEnvio() {
    envio.setPesoKg(new BigDecimal("100"));
    when(vehiculos.findById(1)).thenReturn(Optional.of(vehiculo));
    when(envios.save(envio)).thenReturn(envio);
    assertEquals(new BigDecimal("100"), service.crearEnvio(envio).getPesoKg());
  }

  @Test
  void registrarEnvio_ReferenciasInvalidas_NoPersiste() {
    assertThrows(IllegalArgumentException.class, () -> service.registrarEnvio(null));
    envio.setVehiculo(null);
    assertThrows(IllegalArgumentException.class, () -> service.registrarEnvio(envio));
    envio.setVehiculo(new Vehiculo());
    assertThrows(IllegalArgumentException.class, () -> service.registrarEnvio(envio));
    envio.setVehiculo(vehiculo);
    envio.setConductor(null);
    assertThrows(IllegalArgumentException.class, () -> service.registrarEnvio(envio));
    envio.setConductor(new Conductor());
    assertThrows(IllegalArgumentException.class, () -> service.registrarEnvio(envio));
    verifyNoInteractions(envios, vehiculos);
  }

  @SuppressWarnings("null")
  @Test
  void vehiculoInexistente_NoPersiste() {
    assertThrows(IllegalArgumentException.class, () -> service.crearEnvio(envio));
    verify(envios, never()).save(any());
  }

  @SuppressWarnings("null")
  @Test
  void pesoNulo_NoPersiste() {
    when(vehiculos.findById(1)).thenReturn(Optional.of(vehiculo));
    envio.setPesoKg(null);
    assertThrows(IllegalArgumentException.class, () -> service.crearEnvio(envio));
    verify(envios, never()).save(any());
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @CsvSource({ "0", "-1" })
  void pesoNoPositivo_NoPersiste(String peso) {
    when(vehiculos.findById(1)).thenReturn(Optional.of(vehiculo));
    envio.setPesoKg(new BigDecimal(peso));
    assertThrows(IllegalArgumentException.class, () -> service.crearEnvio(envio));
    verify(envios, never()).save(any());
  }

  @Test
  void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
    invalida("ENTREGADO", "EN_TRANSITO");
  }

  @SuppressWarnings("null")
  void invalida(String antes, String despues) {
    envio.setEstadoEnvio(antes);
    when(envios.findById(3)).thenReturn(Optional.of(envio));
    CambioEstadoDTO dto = new CambioEstadoDTO();
    dto.setNuevoEstado(despues);
    assertThrows(InvalidStateTransitionException.class, () -> service.actualizarEstadoEnvio(3, dto));
    assertEquals(antes, envio.getEstadoEnvio());
    verify(envios, never()).save(any());
    verifyNoInteractions(bitacoras, usuarios);
  }

  @ParameterizedTest
  @CsvSource({ "CANCELADO,PENDIENTE", "ENTREGADO,CANCELADO", "PENDIENTE,ENTREGADO", "PENDIENTE,OTRO",
      "EN_TRANSITO,PENDIENTE", "EN_TRANSITO,EN_TRANSITO" })
  void transicionesNoPermitidas(String antes, String despues) {
    invalida(antes, despues);
  }

  @SuppressWarnings("null")
  @Test
  void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
    envio.setEstadoEnvio("EN_TRANSITO");
    when(envios.findById(3)).thenReturn(Optional.of(envio));
    assertThrows(InvalidStateTransitionException.class, () -> service.cancelarEnvio(3));
    verify(envios, never()).save(any());
    verifyNoInteractions(bitacoras);
  }

  @SuppressWarnings("null")
  void autenticar() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "ignored"));
    Usuario u = new Usuario();
    u.setUsername("admin");
    when(usuarios.findByUsername("admin")).thenReturn(Optional.of(u));
    when(envios.findById(3)).thenReturn(Optional.of(envio));
    when(envios.save(envio)).thenReturn(envio);
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @CsvSource({ "PENDIENTE,EN_TRANSITO", "EN_TRANSITO,ENTREGADO", "PENDIENTE,CANCELADO" })
  void actualizarEstado_Valido_RegistraBitacora(String antes, String despues) {
    autenticar();
    envio.setEstadoEnvio(antes);
    CambioEstadoDTO dto = new CambioEstadoDTO();
    dto.setNuevoEstado(despues);
    dto.setObservaciones("Prueba");
    EnvioResponseDTO result = service.actualizarEstadoEnvio(3, dto);
    assertEquals(despues, result.getEstadoEnvio());
    assertEquals("ABC123", result.getPlacaVehiculo());
    assertEquals("Ana", result.getNombreConductor());
    ArgumentCaptor<BitacoraEnvio> captor = ArgumentCaptor.forClass(BitacoraEnvio.class);
    verify(bitacoras).save(captor.capture());
    BitacoraEnvio b = captor.getValue();
    assertAll(() -> assertSame(envio, b.getEnvio()), () -> assertEquals(antes, b.getEstadoAnterior()),
        () -> assertEquals(despues, b.getEstadoNuevo()), () -> assertEquals("admin", b.getUsuario().getUsername()),
        () -> assertEquals("Prueba", b.getObservaciones()), () -> assertNotNull(b.getFechaCambio()));
  }

  @SuppressWarnings("null")
  @Test
  void cancelarPendiente_RegistraCancelacion() {
    autenticar();
    assertEquals("CANCELADO", service.cancelarEnvio(3).getEstadoEnvio());
    verify(bitacoras).save(any());
  }

  @Test
  void cambioSinRelaciones_MapeaCamposOpcionales() {
    autenticar();
    envio.setVehiculo(null);
    envio.setConductor(null);
    CambioEstadoDTO dto = new CambioEstadoDTO();
    dto.setNuevoEstado("EN_TRANSITO");
    assertNull(service.actualizarEstadoEnvio(3, dto).getPlacaVehiculo());
  }

  @SuppressWarnings("null")
  @Test
  void usuarioInexistente_NoRegistraBitacora() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ausente", "x"));
    when(envios.findById(3)).thenReturn(Optional.of(envio));
    when(envios.save(envio)).thenReturn(envio);
    CambioEstadoDTO dto = new CambioEstadoDTO();
    dto.setNuevoEstado("EN_TRANSITO");
    assertThrows(IllegalArgumentException.class, () -> service.actualizarEstadoEnvio(3, dto));
    verifyNoInteractions(bitacoras);
  }

  @Test
  void envioInexistente_LanzaExcepcion() {
    assertThrows(ResourceNotFoundException.class, () -> service.obtenerEnvioPorId(90));
    assertThrows(ResourceNotFoundException.class, () -> service.actualizarEstadoEnvio(90, new CambioEstadoDTO()));
  }

  @Test
  void obtenerEnvio_MapeaDTO() {
    when(envios.findById(3)).thenReturn(Optional.of(envio));
    assertEquals("EXP-1234", service.obtenerEnvioPorId(3).getCodigoRastreo());
    envio.setVehiculo(null);
    envio.setConductor(null);
    assertNull(service.obtenerEnvioPorId(3).getNombreConductor());
  }

  @Test
  void obtenerEnvios_UsaConsultaOptimizada() {
    when(envios.findAllOptimizados()).thenReturn(List.of(envio));
    assertEquals(List.of(envio), service.obtenerEnviosOptimizados());
    verify(envios).findAllOptimizados();
  }

  @Test
  void bitacora_MapeaTodosLosCampos() {
    Usuario u = new Usuario();
    u.setUsername("admin");
    BitacoraEnvio b = new BitacoraEnvio();
    b.setId(4);
    b.setUsuario(u);
    b.setEstadoAnterior("PENDIENTE");
    b.setEstadoNuevo("EN_TRANSITO");
    b.setFechaCambio(LocalDateTime.of(2026, 9, 14, 10, 0));
    b.setObservaciones("Sale");
    when(bitacoras.findByEnvioId(3)).thenReturn(List.of(b));
    BitacoraResponseDTO dto = service.obtenerBitacoraPorEnvio(3).getFirst();
    assertAll(() -> assertEquals(4, dto.getId()), () -> assertEquals("admin", dto.getUsuario()),
        () -> assertEquals(b.getFechaCambio(), dto.getFechaCambio()),
        () -> assertEquals("PENDIENTE", dto.getEstadoAnterior()),
        () -> assertEquals("EN_TRANSITO", dto.getEstadoNuevo()), () -> assertEquals("Sale", dto.getObservaciones()));
  }

  @Test
  void bitacoraVacia() {
    assertTrue(service.obtenerBitacoraPorEnvio(9).isEmpty());
  }

  @Test
  void actualizarMasivos_Delegacion() {
    service.actualizarEstadosMasivos(1, "EN_TRANSITO");
    verify(envios).updateEstadoByVehiculoId("EN_TRANSITO", 1);
  }

  @ParameterizedTest
  @CsvSource({ "5.0,10.0,2500.0", "15.0,50.0,7500.0", "100.0,2.5,12000.0", "10,20,3000", "1,1,2100" })
  @DisplayName("Debe calcular la tarifa correcta segun peso y distancia")
  void calcularTarifa_CasosVariados_CalculaCorrectamente(double pesoKg, double distanciaKm, double tarifaEsperada) {
    assertEquals(tarifaEsperada, service.calcularTarifa(pesoKg, distanciaKm), 0.01);
    verifyNoInteractions(envios, vehiculos);
  }

  @ParameterizedTest
  @CsvSource({ "0,1", "1,0", "-1,2", "2,-1", "NaN,1", "1,NaN", "Infinity,1", "1,Infinity", "1.7976931348623157E308,1" })
  void tarifa_DatosInvalidos(double peso, double distancia) {
    assertThrows(IllegalArgumentException.class, () -> service.calcularTarifa(peso, distancia));
  }
}
