package com.expresofast.service;

import com.expresofast.model.Envio;
import com.expresofast.dto.*;
import org.springframework.data.domain.Page;
import java.util.List;

public interface EnvioService {
    List<EnvioDTO> obtenerEnvios();
    EnvioDTO obtenerPorRastreo(String codigo);
    EnvioDTO crearEnvio(CrearEnvioDTO dto);
    EnvioDTO actualizarEstado(Integer id, CambioEstadoDTO dto);
    Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir, String busqueda, String estado);
    List<EnvioDTO> listarViaStoredProcedure(String estado);
    EnvioResponseDTO obtenerEnvioPorId(Integer id);
    EnvioResponseDTO crearEnvio(Envio envio);
    EnvioResponseDTO cancelarEnvio(Integer id);
    double calcularTarifa(double pesoKg, double distanciaKm);
    List<Envio> obtenerEnviosOptimizados();
    Envio registrarEnvio(Envio envio);
    EnvioResponseDTO actualizarEstadoEnvio(Integer id, CambioEstadoDTO dto);
    List<BitacoraResponseDTO> obtenerBitacoraPorEnvio(Integer envioId);
    void actualizarEstadosMasivos(Integer vehiculoId, String nuevoEstado);
}
