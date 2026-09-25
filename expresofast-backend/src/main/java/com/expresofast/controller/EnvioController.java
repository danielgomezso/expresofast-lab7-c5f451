package com.expresofast.controller;

import com.expresofast.service.EnvioService;
import com.expresofast.model.Envio;
import com.expresofast.dto.BitacoraResponseDTO;
import com.expresofast.dto.CambioEstadoDTO;
import com.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;
import com.expresofast.dto.CrearEnvioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.expresofast.dto.EnvioDTO;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/v1/envios")
    public List<EnvioDTO> obtenerEnvios() {
        return envioService.obtenerEnvios();
    }

    @GetMapping("/v1/envios/rastreo/{codigo}")
    public EnvioDTO obtenerPorRastreo(@PathVariable String codigo) {
        return envioService.obtenerPorRastreo(codigo);
    }

    @PostMapping("/v1/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioDTO crearEnvio(@Valid @RequestBody CrearEnvioDTO dto) {
        return envioService.crearEnvio(dto);
    }

    @PatchMapping("/v1/envios/{id}/estado")
    public EnvioDTO actualizarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoDTO dto) {
        return envioService.actualizarEstado(id, dto);
    }

    @GetMapping("/v1/envios/paginados")
    public Page<EnvioDTO> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "") String estado) {
        return envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado);
    }

    @GetMapping("/v1/envios/procedimiento/{estado}")
    public List<EnvioDTO> listarViaStoredProcedure(@PathVariable String estado) {
        return envioService.listarViaStoredProcedure(estado);
    }

    @GetMapping("/envios/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvio(@PathVariable @NonNull Integer id) {
        return ResponseEntity.ok(envioService.obtenerEnvioPorId(id));
    }

    @GetMapping("/envios/optimizados")
    public ResponseEntity<List<Envio>> obtenerEnviosOptimizados() {
        List<Envio> envios = envioService.obtenerEnviosOptimizados();
        return ResponseEntity.ok(envios);
    }

    @PostMapping("/envios")
    public ResponseEntity<Envio> registrarEnvio(@org.springframework.validation.annotation.Validated(Envio.RegistroAnterior.class) @RequestBody Envio envio) {
        Envio nuevoEnvio = envioService.registrarEnvio(envio);
        return new ResponseEntity<>(nuevoEnvio, HttpStatus.CREATED);
    }

    @PatchMapping("/envios/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstadoEnvio(
            @PathVariable Integer id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        EnvioResponseDTO envioActualizado = envioService.actualizarEstadoEnvio(id, dto);
        return ResponseEntity.ok(envioActualizado);
    }

    @GetMapping("/envios/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacoraPorEnvio(id));
    }
}
