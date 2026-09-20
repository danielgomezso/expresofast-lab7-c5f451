package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {
    @Mock
    EmpresaLogisticaRepository repository;
    @InjectMocks
    EmpresaLogisticaService service;
    EmpresaLogistica e;

    @BeforeEach
    void setup() {
        e = new EmpresaLogistica();
        e.setNombre("Expreso");
        e.setCedulaJuridica("3-101-123456");
    }

    @SuppressWarnings("null")
    @Test
    void registrarEmpresa_Valida_PersisteFecha() {
        when(repository.save(e)).thenReturn(e);
        assertSame(e, service.registrarEmpresa(e));
        assertNotNull(e.getFechaRegistro());
        verify(repository).save(e);
    }

    @SuppressWarnings("null")
    @Test
    void cedulaDuplicada_NoPersiste() {
        when(repository.existsByCedulaJuridica(e.getCedulaJuridica())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.registrarEmpresa(e));
        verify(repository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void nombreDuplicado_NoPersiste() {
        when(repository.existsByNombre("Expreso")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.registrarEmpresa(e));
        verify(repository, never()).save(any());
    }

    @Test
    void datosInvalidos_NoPersiste() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarEmpresa(null));
        e.setNombre(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrarEmpresa(e));
        e.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> service.registrarEmpresa(e));
        e.setNombre("Expreso");
        e.setCedulaJuridica(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrarEmpresa(e));
        e.setCedulaJuridica(" ");
        assertThrows(IllegalArgumentException.class, () -> service.registrarEmpresa(e));
        verifyNoInteractions(repository);
    }

    @Test
    void obtenerExistente() {
        when(repository.findById(1)).thenReturn(Optional.of(e));
        assertSame(e, service.obtenerEmpresa(1));
    }

    @Test
    void obtenerInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> service.obtenerEmpresa(1));
    }

    @Test
    void listarEmpresas() {
        when(repository.findAll()).thenReturn(List.of(e));
        assertEquals(List.of(e), service.listarEmpresas());
    }

    @Test
    void listarVacias() {
        assertTrue(service.listarEmpresas().isEmpty());
    }
}
