package cr.ac.ucr.paraiso.ie.c5f451.expresofast.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
        "spring.jpa.properties.hibernate.query.fail_on_pagination_over_collection_fetch=true"
})
class EnvioRepositoryTest {
    @Autowired EnvioRepository repository;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void datos() {
        jdbc.update("INSERT INTO EmpresaLogistica (empresa_id,nombre,cedula_juridica,telefono,fecha_registro) VALUES (100,'Prueba','TEST','22220000',CURRENT_TIMESTAMP)");
        jdbc.update("INSERT INTO Conductor (conductor_id,nombre,apellidos,licencia,telefono,activo) VALUES (100,'Ana','Mora','TEST','88880000',true)");
        jdbc.update("INSERT INTO Vehiculo (vehiculo_id,placa,capacidad_kg,estado,empresa_id) VALUES (100,'TEST',1000,'DISPONIBLE',100)");
        String[] estados = {"PENDIENTE", "EN_TRANSITO", "ENTREGADO", "CANCELADO"};
        for (int i = 1; i <= 16; i++) {
            jdbc.update("INSERT INTO Envio (codigo_rastreo,destinatario,direccion_destino,peso_kg,costo,estado_envio,vehiculo_id,conductor_id,fecha_creacion) VALUES (?,?,?,?,?,?,100,100,CURRENT_TIMESTAMP)",
                    "EXP-" + (9000 + i), "Cliente " + i, "Cartago", 5, 2000 + i, estados[(i - 1) % 4]);
        }
    }

    @Test
    void primeraIntermediaUltimaYFueraDeRango() {
        var orden = Sort.by("codigoRastreo");
        var primera = repository.buscarPaginado("", "", PageRequest.of(0, 5, orden));
        var segunda = repository.buscarPaginado("", "", PageRequest.of(1, 5, orden));
        var ultima = repository.buscarPaginado("", "", PageRequest.of(3, 5, orden));
        assertEquals(16, primera.getTotalElements());
        assertEquals(4, primera.getTotalPages());
        assertEquals(5, primera.getNumberOfElements());
        assertEquals("EXP-9001", primera.getContent().getFirst().codigoRastreo());
        assertEquals("EXP-9006", segunda.getContent().getFirst().codigoRastreo());
        assertTrue(primera.isFirst());
        assertFalse(segunda.isFirst());
        assertFalse(segunda.isLast());
        assertEquals(1, ultima.getNumberOfElements());
        assertTrue(ultima.isLast());
        assertTrue(repository.buscarPaginado("", "", PageRequest.of(8, 5, orden)).isEmpty());
    }

    @Test
    void filtraPorTextoYEstadoEnBaseDeDatos() {
        var pagina = PageRequest.of(0, 10, Sort.by("costo").descending());
        var resultado = repository.buscarPaginado("cLiEnTe", "PENDIENTE", pagina);
        assertEquals(4, resultado.getTotalElements());
        assertEquals("EXP-9013", resultado.getContent().getFirst().codigoRastreo());
        assertTrue(resultado.stream().allMatch(e -> e.estado().equals("PENDIENTE")));
        assertEquals(16, repository.buscarPaginado("cartago", "", pagina).getTotalElements());
        assertEquals(1, repository.buscarPaginado("EXP-9016", "CANCELADO", pagina).getTotalElements());
        assertTrue(repository.buscarPaginado("inexistente", "", pagina).isEmpty());
    }
}
