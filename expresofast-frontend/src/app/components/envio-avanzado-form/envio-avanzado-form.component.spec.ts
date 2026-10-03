import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { EnvioAvanzadoFormComponent } from './envio-avanzado-form.component';
import { environment } from '../../../environments/environment';

describe('EnvioAvanzadoFormComponent', () => {
  let componente: EnvioAvanzadoFormComponent;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EnvioAvanzadoFormComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    componente = TestBed.createComponent(EnvioAvanzadoFormComponent).componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('agrega y elimina paquetes conservando al menos uno', () => {
    componente.eliminarPaquete(0);
    expect(componente.paquetes.length).toBe(1);
    componente.agregarPaquete();
    expect(componente.paquetes.length).toBe(2);
    componente.eliminarPaquete(0);
    expect(componente.paquetes.length).toBe(1);
  });

  it('rechaza fechas iguales o anteriores y acepta una entrega posterior', () => {
    componente.formulario.patchValue({ fechaDespacho: '2026-10-02', fechaEntregaEstimada: '2026-10-02' });
    expect(componente.formulario.hasError('fechasInvalidas')).toBe(true);
    componente.formulario.controls.fechaEntregaEstimada.setValue('2026-10-01');
    expect(componente.formulario.hasError('fechasInvalidas')).toBe(true);
    componente.formulario.controls.fechaEntregaEstimada.setValue('2026-10-03');
    expect(componente.formulario.hasError('fechasInvalidas')).toBe(false);
  });

  it('envía el formulario válido con sus paquetes y lo reinicia', async () => {
    componente.formulario.patchValue({
      numeroTracking: 'EXP-NUEVO', destinatario: 'Ana', direccionDestino: 'Cartago',
      montoFlete: 2500, fechaDespacho: '2026-10-02', fechaEntregaEstimada: '2026-10-03',
      paquetes: [{ descripcion: 'Libros', pesoKg: 1.25 }],
    });
    await new Promise(resolve => setTimeout(resolve, 450));
    http.expectOne(environment.API_URL + 'envios/check-tracking/EXP-NUEVO').flush(false);
    expect(componente.formulario.valid).toBe(true);
    componente.guardar();
    const request = http.expectOne(environment.API_URL + 'envios/registro');
    expect(request.request.body.paquetes).toEqual([{ descripcion: 'Libros', pesoKg: 1.25 }]);
    request.flush({ codigoRastreo: 'EXP-NUEVO' });
    expect(componente.codigo()).toBe('EXP-NUEVO');
    expect(componente.paquetes.length).toBe(1);
    expect(componente.formulario.controls.numeroTracking.value).toBe('');
    expect(componente.guardando()).toBe(false);
  });

  it('bloquea rastreos existentes y permite reintentar si falla la API', async () => {
    const tracking = componente.formulario.controls.numeroTracking;
    tracking.setValue('EXP-PRUEBA');
    expect(componente.formulario.pending).toBe(true);
    componente.guardar();
    http.expectNone(environment.API_URL + 'envios/registro');
    await new Promise(resolve => setTimeout(resolve, 450));
    http.expectOne(environment.API_URL + 'envios/check-tracking/EXP-PRUEBA').flush(true);
    expect(tracking.hasError('trackingTomado')).toBe(true);
    tracking.setValue('EXP-LIBRE');
    await new Promise(resolve => setTimeout(resolve, 450));
    http.expectOne(environment.API_URL + 'envios/check-tracking/EXP-LIBRE').flush({}, { status: 500, statusText: 'Error' });
    expect(tracking.hasError('trackingNoVerificado')).toBe(true);
    componente.verificarTracking();
    await new Promise(resolve => setTimeout(resolve, 450));
    http.expectOne(environment.API_URL + 'envios/check-tracking/EXP-LIBRE').flush(false);
    expect(tracking.valid).toBe(true);
  });
});
