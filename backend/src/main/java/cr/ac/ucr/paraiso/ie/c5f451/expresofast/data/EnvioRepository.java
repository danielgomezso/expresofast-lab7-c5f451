package cr.ac.ucr.paraiso.ie.c5f451.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.EnvioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.Procedure;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    @Procedure(name = "Envio.porEstado")
    List<Envio> obtenerPorEstado(@Param("pEstado") String estado);

    @Query("""
            SELECT new cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.EnvioDTO(
                e.id, e.codigoRastreo, e.destinatario, e.direccionDestino,
                e.costo, e.estadoEnvio, e.fechaCreacion)
            FROM Envio e
            WHERE (:estado = '' OR e.estadoEnvio = :estado)
            AND (:busqueda = '' OR LOWER(e.codigoRastreo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(e.destinatario) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<EnvioDTO> buscarPaginado(@Param("busqueda") String busqueda,
            @Param("estado") String estado, Pageable pageable);

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo v JOIN FETCH v.empresa JOIN FETCH e.conductor")
    List<Envio> findAllOptimizados();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    int updateEstadoByVehiculoId(@Param("nuevoEstado") String nuevoEstado, @Param("vehiculoId") Integer vehiculoId);
}
