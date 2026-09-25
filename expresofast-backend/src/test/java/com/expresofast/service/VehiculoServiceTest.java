package com.expresofast.service;

import com.expresofast.repository.*;
import com.expresofast.model.*;
import com.expresofast.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {
    @Mock
    VehiculoRepository vehiculos;
    @Mock
    ConductorRepository conductores;
    @InjectMocks
    VehiculoService service;
    Vehiculo v;

    @BeforeEach
    void setup() {
        v = new Vehiculo();
        v.setPlaca("ABC123");
        v.setCapacidadKg(BigDecimal.TEN);
    }

    @SuppressWarnings("null")
    @Test
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        when(vehiculos.existsByPlaca("ABC123")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.registrarVehiculo(v));
        verify(vehiculos, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void registrarVehiculo_Valido_Persiste() {
        when(vehiculos.save(v)).thenReturn(v);
        assertSame(v, service.registrarVehiculo(v));
        verify(vehiculos).save(v);
    }

    @Test
    void placaInvalida_NoPersiste() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarVehiculo(null));
        v.setPlaca(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrarVehiculo(v));
        v.setPlaca(" ");
        assertThrows(IllegalArgumentException.class, () -> service.registrarVehiculo(v));
        verifyNoInteractions(vehiculos);
    }

    @SuppressWarnings("null")
    @Test
    void capacidadInvalida_NoPersiste() {
        for (BigDecimal capacidad : new BigDecimal[] { null, BigDecimal.ZERO, BigDecimal.ONE.negate() }) {
            v.setCapacidadKg(capacidad);
            assertThrows(IllegalArgumentException.class, () -> service.registrarVehiculo(v));
        }
        verify(vehiculos, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        Conductor c = new Conductor();
        c.setActivo(false);
        when(vehiculos.findById(1)).thenReturn(Optional.of(v));
        when(conductores.findById(2)).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () -> service.asignarConductor(1, 2));
        assertNull(v.getConductor());
        verify(vehiculos, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void asignarConductor_Activo_PersisteRelacion() {
        Conductor c = new Conductor();
        when(vehiculos.findById(1)).thenReturn(Optional.of(v));
        when(conductores.findById(2)).thenReturn(Optional.of(c));
        when(vehiculos.save(v)).thenReturn(v);
        assertSame(c, service.asignarConductor(1, 2).getConductor());
        verify(vehiculos).save(v);
    }

    @Test
    void vehiculoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> service.asignarConductor(1, 2));
        verifyNoInteractions(conductores);
    }

    @SuppressWarnings("null")
    @Test
    void conductorInexistente() {
        when(vehiculos.findById(1)).thenReturn(Optional.of(v));
        assertThrows(ResourceNotFoundException.class, () -> service.asignarConductor(1, 2));
        verify(vehiculos, never()).save(any());
    }
}
