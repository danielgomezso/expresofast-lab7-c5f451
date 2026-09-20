package cr.ac.ucr.paraiso.ie.c5f451.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.BitacoraEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {
    List<BitacoraEnvio> findByEnvioId(Integer envioId);
}