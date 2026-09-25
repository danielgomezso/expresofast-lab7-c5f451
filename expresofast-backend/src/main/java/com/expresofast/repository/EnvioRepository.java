package com.expresofast.repository;

import com.expresofast.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.expresofast.dto.EnvioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.Procedure;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    java.util.Optional<Envio> findByCodigoRastreo(String codigoRastreo);

    List<Envio> findByEstadoEnvio(String estado);

    @Procedure(name = "Envio.porEstado")
    List<Envio> obtenerPorEstado(@Param("pEstado") String estado);

    @Query("""
            SELECT new com.expresofast.dto.EnvioDTO(
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

    @Query("SELECT e FROM Envio e LEFT JOIN FETCH e.vehiculo v LEFT JOIN FETCH v.empresa LEFT JOIN FETCH e.conductor")
    List<Envio> findAllOptimizados();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    int updateEstadoByVehiculoId(@Param("nuevoEstado") String nuevoEstado, @Param("vehiculoId") Integer vehiculoId);
}
