package cr.ac.ucr.paraiso.ie.c5f451.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
}