package com.expresofast.repository;

import com.expresofast.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    boolean existsByPlaca(String placa);
}