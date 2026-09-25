package com.expresofast.service;

import com.expresofast.repository.EnvioRepository;
import com.expresofast.repository.VehiculoRepository;
import com.expresofast.repository.BitacoraEnvioRepository;
import com.expresofast.repository.UsuarioRepository;
import com.expresofast.model.Envio;
import com.expresofast.model.Vehiculo;
import com.expresofast.model.BitacoraEnvio;
import com.expresofast.model.Usuario;
import com.expresofast.dto.BitacoraResponseDTO;
import com.expresofast.dto.CambioEstadoDTO;
import com.expresofast.dto.EnvioResponseDTO;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import com.expresofast.dto.CrearEnvioDTO;
import com.expresofast.exception.*;
import java.util.List;
import java.util.Locale;
import com.expresofast.dto.EnvioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class EnvioServiceImpl implements EnvioService {

    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerEnvios() {
        return envioRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"))
                .stream().map(this::toEnvioDTO).toList();
    }

    @Transactional(readOnly = true)
    public EnvioDTO obtenerPorRastreo(String codigo) {
        return toEnvioDTO(envioRepository.findByCodigoRastreo(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("No existe un envío con ese código.")));
    }

    @Transactional
    public EnvioDTO crearEnvio(CrearEnvioDTO dto) {
        Envio envio = new Envio();
        envio.setCodigoRastreo("EXP-" + Year.now() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT));
        envio.setDestinatario(dto.destinatario().trim());
        envio.setDireccionDestino(dto.direccionDestino().trim());
        envio.setCosto(dto.montoFlete());
        envio.setEstadoEnvio("PENDIENTE");
        return toEnvioDTO(envioRepository.save(envio));
    }

    @Transactional
    public EnvioDTO actualizarEstado(Integer id, CambioEstadoDTO dto) {
        actualizarEstadoEnvio(id, dto);
        return toEnvioDTO(envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El envío no existe.")));
    }

    private EnvioDTO toEnvioDTO(Envio envio) {
        return new EnvioDTO(envio.getId(), envio.getCodigoRastreo(), envio.getDestinatario(),
                envio.getDireccionDestino(), envio.getCosto(), envio.getEstadoEnvio(), envio.getFechaCreacion());
    }

    @Transactional(readOnly = true)
    public Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir,
            String busqueda, String estado) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("La página debe ser mayor o igual a 0 y el tamaño entre 1 y 100.");
        }
        String campo = switch (sortBy) {
            case "id", "codigoRastreo", "destinatario", "direccionDestino", "fechaCreacion" -> sortBy;
            case "montoFlete" -> "costo";
            case "estado" -> "estadoEnvio";
            default -> throw new IllegalArgumentException("Campo de ordenamiento inválido.");
        };
        Sort orden = Sort.by(Sort.Direction.fromString(dir), campo).and(Sort.by("id"));
        return envioRepository.buscarPaginado(busqueda.trim(), validarEstado(estado),
                PageRequest.of(page, size, orden));
    }

    @Transactional(readOnly = true)
    public List<EnvioDTO> listarViaStoredProcedure(String estado) {
        String filtro = validarEstado(estado);
        if (filtro.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un estado.");
        }
        return envioRepository.obtenerPorEstado(filtro).stream()
                .map(e -> new EnvioDTO(e.getId(), e.getCodigoRastreo(), e.getDestinatario(),
                        e.getDireccionDestino(), e.getCosto(), e.getEstadoEnvio(), e.getFechaCreacion()))
                .toList();
    }

    private String validarEstado(String estado) {
        String valor = estado.trim().toUpperCase(Locale.ROOT);
        if (!List.of("", "PENDIENTE", "EN_TRANSITO", "ENTREGADO", "CANCELADO").contains(valor)) {
            throw new IllegalArgumentException("Estado de envío inválido.");
        }
        return valor;
    }

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;

    private final BitacoraEnvioRepository bitacoraEnvioRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository,
            UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerEnvioPorId(@NonNull Integer id) {
        return toDTO(
                envioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("El envío no existe.")));
    }

    @Transactional
    public EnvioResponseDTO crearEnvio(Envio envio) {
        return toDTO(registrarEnvio(envio));
    }

    @Transactional
    public EnvioResponseDTO cancelarEnvio(Integer id) {
        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("CANCELADO");
        return actualizarEstadoEnvio(id, dto);
    }

    // Regla provisional: mayor entre componente de peso y componente de distancia.
    public double calcularTarifa(double pesoKg, double distanciaKm) {
        if (!Double.isFinite(pesoKg) || !Double.isFinite(distanciaKm) || pesoKg <= 0 || distanciaKm <= 0)
            throw new IllegalArgumentException("Peso y distancia deben ser positivos y finitos.");
        double tarifa = Math.max(2000.0 + 100.0 * pesoKg, 150.0 * distanciaKm);
        if (!Double.isFinite(tarifa))
            throw new IllegalArgumentException("Tarifa fuera de rango.");
        return tarifa;
    }

    private EnvioResponseDTO toDTO(Envio envio) {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setId(envio.getId());
        dto.setCodigoRastreo(envio.getCodigoRastreo());
        dto.setDireccionDestino(envio.getDireccionDestino());
        dto.setPesoKg(envio.getPesoKg());
        dto.setCosto(envio.getCosto());
        dto.setEstadoEnvio(envio.getEstadoEnvio());
        if (envio.getVehiculo() != null)
            dto.setPlacaVehiculo(envio.getVehiculo().getPlaca());
        if (envio.getConductor() != null)
            dto.setNombreConductor(envio.getConductor().getNombre());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<Envio> obtenerEnviosOptimizados() {
        return envioRepository.findAllOptimizados();
    }

    @Transactional
    public Envio registrarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envío es obligatorio.");
        }

        if (envio.getVehiculo() == null || envio.getVehiculo().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un vehículo válido.");
        }

        if (envio.getConductor() == null || envio.getConductor().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un conductor válido.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(envio.getVehiculo().getId())
                .orElseThrow(() -> new IllegalArgumentException("El vehículo especificado no existe."));

        if (envio.getPesoKg() == null || envio.getPesoKg().signum() <= 0)
            throw new IllegalArgumentException("El peso debe ser positivo.");
        if (envio.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new CapacidadExcedidaException("Error: El peso del envío (" + envio.getPesoKg() +
                    " kg) supera la capacidad máxima del vehículo asignado (" +
                    vehiculo.getCapacidadKg() + " kg).");
        }

        envio.setEstadoEnvio("PENDIENTE");
        return envioRepository.save(envio);
    }

    @Transactional
    public EnvioResponseDTO actualizarEstadoEnvio(Integer id, CambioEstadoDTO dto) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El envío no existe."));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = dto.getNuevoEstado();

        boolean permitida = ("PENDIENTE".equals(estadoAnterior) &&
                ("EN_TRANSITO".equals(estadoNuevo) || "CANCELADO".equals(estadoNuevo))) ||
                ("EN_TRANSITO".equals(estadoAnterior) && "ENTREGADO".equals(estadoNuevo));
        if (!permitida) {
            throw new com.expresofast.exception.InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío " + envio.getCodigoRastreo());
        }

        envio.setEstadoEnvio(estadoNuevo);
        envio = envioRepository.save(envio);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuario);
        bitacora.setObservaciones(dto.getObservaciones());

        bitacoraEnvioRepository.save(bitacora);

        EnvioResponseDTO response = new EnvioResponseDTO();
        response.setId(envio.getId());
        response.setCodigoRastreo(envio.getCodigoRastreo());
        response.setDireccionDestino(envio.getDireccionDestino());
        response.setPesoKg(envio.getPesoKg());
        response.setCosto(envio.getCosto());
        response.setEstadoEnvio(envio.getEstadoEnvio());

        if (envio.getVehiculo() != null) {
            response.setPlacaVehiculo(envio.getVehiculo().getPlaca());
        }
        if (envio.getConductor() != null) {
            response.setNombreConductor(envio.getConductor().getNombre());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacoraPorEnvio(Integer envioId) {
        return bitacoraEnvioRepository.findByEnvioId(envioId).stream().map(b -> {
            BitacoraResponseDTO dto = new BitacoraResponseDTO();
            dto.setId(b.getId());
            dto.setEstadoAnterior(b.getEstadoAnterior());
            dto.setEstadoNuevo(b.getEstadoNuevo());
            dto.setFechaCambio(b.getFechaCambio());
            dto.setUsuario(b.getUsuario().getUsername());
            dto.setObservaciones(b.getObservaciones());
            return dto;
        }).toList();
    }

    @Transactional
    public void actualizarEstadosMasivos(Integer vehiculoId, String nuevoEstado) {
        envioRepository.updateEstadoByVehiculoId(nuevoEstado, vehiculoId);
    }
}
