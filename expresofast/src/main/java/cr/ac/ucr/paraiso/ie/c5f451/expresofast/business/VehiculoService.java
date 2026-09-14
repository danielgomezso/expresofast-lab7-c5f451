package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {
    private final VehiculoRepository vehiculos;
    private final ConductorRepository conductores;

    public VehiculoService(VehiculoRepository vehiculos, ConductorRepository conductores) {
        this.vehiculos = vehiculos;
        this.conductores = conductores;
    }

    @Transactional
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getPlaca() == null || vehiculo.getPlaca().isBlank())
            throw new IllegalArgumentException("La placa es obligatoria.");
        if (vehiculos.existsByPlaca(vehiculo.getPlaca()))
            throw new DuplicateResourceException("La placa ya existe.");
        if (vehiculo.getCapacidadKg() == null || vehiculo.getCapacidadKg().signum() <= 0)
            throw new IllegalArgumentException("La capacidad debe ser positiva.");
        return vehiculos.save(vehiculo);
    }

    @Transactional
    public Vehiculo asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = vehiculos.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado."));
        Conductor conductor = conductores.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado."));
        if (!conductor.isActivo())
            throw new IllegalArgumentException("El conductor está inactivo.");
        vehiculo.setConductor(conductor);
        return vehiculos.save(vehiculo);
    }
}
