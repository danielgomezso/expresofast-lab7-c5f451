package cr.ac.ucr.paraiso.ie.c5f451.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo v JOIN FETCH v.empresa JOIN FETCH e.conductor")
    List<Envio> findAllOptimizados();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    int updateEstadoByVehiculoId(@Param("nuevoEstado") String nuevoEstado, @Param("vehiculoId") Integer vehiculoId);
}