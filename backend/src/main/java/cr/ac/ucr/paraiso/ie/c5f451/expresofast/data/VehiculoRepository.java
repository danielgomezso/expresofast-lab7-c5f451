package cr.ac.ucr.paraiso.ie.c5f451.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    boolean existsByPlaca(String placa);
}