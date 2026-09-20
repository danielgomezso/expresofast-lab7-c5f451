package cr.ac.ucr.paraiso.ie.c5f451.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}