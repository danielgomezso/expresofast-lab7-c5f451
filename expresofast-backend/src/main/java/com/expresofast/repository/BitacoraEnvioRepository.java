package com.expresofast.repository;

import com.expresofast.model.BitacoraEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {
    List<BitacoraEnvio> findByEnvioId(Integer envioId);
}