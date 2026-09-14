package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto.*;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    AuthService service;

    @Test
    void loginValido_GeneraTokenYRoles() {
        AuthRequestDTO req = new AuthRequestDTO();
        req.setUsername("admin");
        req.setPassword("test");
        var auth = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt-prueba");
        AuthResponseDTO result = service.login(req);
        assertEquals("jwt-prueba", result.getToken());
        assertEquals("admin", result.getUsername());
        assertEquals(List.of("ROLE_ADMIN"), result.getRoles());
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor
                .forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("admin", captor.getValue().getPrincipal());
        assertEquals("test", captor.getValue().getCredentials());
    }

    @Test
    void loginInvalido_NoGeneraToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));
        assertThrows(BadCredentialsException.class, () -> service.login(new AuthRequestDTO()));
        verifyNoInteractions(jwtTokenProvider);
    }
}
