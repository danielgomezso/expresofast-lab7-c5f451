package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.repository.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.repository.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.EnvioResponseDTO;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.*;
import java.util.List;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;

    private final BitacoraEnvioRepository bitacoraEnvioRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioService(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository,
            UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerEnvioPorId(Integer id) {
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
            throw new cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.InvalidStateTransitionException(
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