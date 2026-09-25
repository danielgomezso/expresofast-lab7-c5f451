package com.expresofast.controller;

import com.expresofast.service.VehiculoService;
import com.expresofast.model.Vehiculo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    public ResponseEntity<Vehiculo> registrarVehiculo(@RequestBody Vehiculo vehiculo) {
        Vehiculo nuevo = vehiculoService.registrarVehiculo(vehiculo);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/conductor")
    public ResponseEntity<Vehiculo> asignarConductor(
            @PathVariable Integer id,
            @RequestBody AsignarConductorDTO dto) {
        Vehiculo actualizado = vehiculoService.asignarConductor(id, dto.conductorId());
        return ResponseEntity.ok(actualizado);
    }

    public record AsignarConductorDTO(Integer conductorId) {
    }
}