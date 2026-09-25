package com.expresofast.repository;

import com.expresofast.model.EmpresaLogistica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaLogisticaRepository extends JpaRepository<EmpresaLogistica, Integer> {
    boolean existsByCedulaJuridica(String cedulaJuridica);
    boolean existsByNombre(String nombre);
}