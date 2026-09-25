import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EnvioService } from './envio.service';
import { AuthService, authInterceptor } from './auth.service';
import { environment } from '../../environments/environment';

describe('EnvioService', () => {
  let servicio: EnvioService;
  let http: HttpTestingController;
  const url = environment.API_URL + 'envios';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(EnvioService);
    http = TestBed.inject(HttpTestingController);
    TestBed.inject(AuthService).token.set('token-prueba');
  });

  afterEach(() => http.verify());

  it('consulta la lista con JWT', () => {
    servicio.obtenerEnvios().subscribe(envios => expect(envios).toEqual([]));
    const request = http.expectOne(url);
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-prueba');
    request.flush([]);
  });

  it('envía los tres datos de registro', () => {
    const payload = { destinatario: 'Ana', direccionDestino: 'Cartago', montoFlete: 2500 };
    servicio.crearEnvio(payload).subscribe();
    const request = http.expectOne(url);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ id: 1, ...payload, estado: 'PENDIENTE' });
  });

  it('consulta el código sin espacios y codifica la URL', () => {
    servicio.obtenerPorRastreo(' EXP-2026/A ').subscribe();
    const request = http.expectOne(url + '/rastreo/EXP-2026%2FA');
    expect(request.request.method).toBe('GET');
    request.flush({});
  });

  it('actualiza el estado mediante PATCH', () => {
    servicio.actualizarEstado(1, 'EN_TRANSITO').subscribe();
    const request = http.expectOne(url + '/1/estado');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ nuevoEstado: 'EN_TRANSITO' });
    request.flush({ id: 1, estado: 'EN_TRANSITO' });
  });
});
