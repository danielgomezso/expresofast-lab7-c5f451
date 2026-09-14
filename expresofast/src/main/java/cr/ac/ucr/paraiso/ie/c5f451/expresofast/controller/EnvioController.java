package cr.ac.ucr.paraiso.ie.c5f451.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<Envio>> obtenerEnviosOptimizados() {
        List<Envio> envios = envioService.obtenerEnviosOptimizados();
        return ResponseEntity.ok(envios);
    }

    @PostMapping
    public ResponseEntity<Envio> registrarEnvio(@Valid @RequestBody Envio envio) {
        Envio nuevoEnvio = envioService.registrarEnvio(envio);
        return new ResponseEntity<>(nuevoEnvio, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstadoEnvio(
            @PathVariable Integer id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        EnvioResponseDTO envioActualizado = envioService.actualizarEstadoEnvio(id, dto);
        return ResponseEntity.ok(envioActualizado);
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacoraPorEnvio(id));
    }
}