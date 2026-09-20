package cr.ac.ucr.paraiso.ie.c5f451.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.EmpresaLogistica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaLogisticaRepository extends JpaRepository<EmpresaLogistica, Integer> {
    boolean existsByCedulaJuridica(String cedulaJuridica);
    boolean existsByNombre(String nombre);
}