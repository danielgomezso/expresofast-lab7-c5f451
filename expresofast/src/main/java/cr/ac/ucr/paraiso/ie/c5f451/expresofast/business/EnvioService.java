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

        if (envio.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException("Error: El peso del envío (" + envio.getPesoKg() +
                    " kg) supera la capacidad máxima del vehículo asignado (" +
                    vehiculo.getCapacidadKg() + " kg).");
        }

        return envioRepository.save(envio);
    }

    @Transactional
    public EnvioResponseDTO actualizarEstadoEnvio(Integer id, CambioEstadoDTO dto) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El envío no existe."));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = dto.getNuevoEstado();

        if ((estadoAnterior.equals("ENTREGADO") || estadoAnterior.equals("CANCELADO")) &&
                (estadoNuevo.equals("PENDIENTE") || estadoNuevo.equals("EN_TRANSITO"))) {
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