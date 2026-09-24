package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.EnvioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioPaginacionServiceTest {
    @Mock EnvioRepository repository;
    @InjectMocks EnvioService service;

    @ParameterizedTest
    @CsvSource({"id,id", "codigoRastreo,codigoRastreo", "destinatario,destinatario",
            "direccionDestino,direccionDestino", "fechaCreacion,fechaCreacion", "montoFlete,costo", "estado,estadoEnvio"})
    void paginaConFiltrosYOrden(String campo, String propiedad) {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, propiedad).and(Sort.by("id")));
        EnvioDTO dto = new EnvioDTO(1, "EXP-9001", "Ana", "Cartago", new BigDecimal("2500"),
                "PENDIENTE", LocalDateTime.of(2026, 9, 1, 10, 0));
        when(repository.buscarPaginado("Ana", "PENDIENTE", pageable))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 16));
        Page<EnvioDTO> resultado = service.listarPaginado(1, 5, campo, "desc", " Ana ", " pendiente ");
        assertEquals(16, resultado.getTotalElements());
        assertEquals(4, resultado.getTotalPages());
        assertEquals(1, resultado.getNumber());
        assertEquals(dto, resultado.getContent().getFirst());
        assertFalse(resultado.isFirst());
        assertFalse(resultado.isLast());
    }

    @ParameterizedTest
    @CsvSource({"-1,5,id,ASC,PENDIENTE", "0,0,id,ASC,PENDIENTE", "0,101,id,ASC,PENDIENTE",
            "0,5,password,ASC,PENDIENTE", "0,5,id,OTRO,PENDIENTE", "0,5,id,ASC,OTRO"})
    void rechazaParametrosInvalidos(int page, int size, String sort, String dir, String estado) {
        assertThrows(IllegalArgumentException.class, () -> service.listarPaginado(page, size, sort, dir, "", estado));
        verifyNoInteractions(repository);
    }

    @Test
    void paginaVaciaSinFiltro() {
        when(repository.buscarPaginado(eq(""), eq(""), any(Pageable.class))).thenReturn(Page.empty());
        assertTrue(service.listarPaginado(0, 5, "fechaCreacion", "ASC", "", "").isEmpty());
    }

    @Test
    void procedimientoMapeaDTO() {
        Envio envio = new Envio();
        envio.setId(1);
        envio.setCodigoRastreo("EXP-9001");
        envio.setDestinatario("Ana");
        envio.setDireccionDestino("Cartago");
        envio.setCosto(new BigDecimal("2500"));
        envio.setEstadoEnvio("PENDIENTE");
        envio.setFechaCreacion(LocalDateTime.of(2026, 9, 1, 10, 0));
        when(repository.obtenerPorEstado("PENDIENTE")).thenReturn(List.of(envio));
        assertEquals(new EnvioDTO(1, "EXP-9001", "Ana", "Cartago", new BigDecimal("2500"),
                "PENDIENTE", envio.getFechaCreacion()), service.listarViaStoredProcedure("pendiente").getFirst());
    }

    @Test
    void procedimientoSinResultadosYEstadoInvalido() {
        when(repository.obtenerPorEstado("CANCELADO")).thenReturn(List.of());
        assertTrue(service.listarViaStoredProcedure("CANCELADO").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.listarViaStoredProcedure(""));
        assertThrows(IllegalArgumentException.class, () -> service.listarViaStoredProcedure("OTRO"));
    }
}
