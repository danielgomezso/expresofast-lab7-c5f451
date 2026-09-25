import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CrearEnvioPayload, Envio, EstadoEnvio } from '../models/envio.model';

@Injectable({ providedIn: 'root' })
export class EnvioService {
  private http = inject(HttpClient);
  private url = environment.API_URL + 'envios';

  obtenerEnvios() {
    return this.http.get<Envio[]>(this.url);
  }

  obtenerPorRastreo(codigo: string) {
    return this.http.get<Envio>(`${this.url}/rastreo/${encodeURIComponent(codigo.trim())}`);
  }

  crearEnvio(payload: CrearEnvioPayload) {
    return this.http.post<Envio>(this.url, payload);
  }

  actualizarEstado(id: number, nuevoEstado: EstadoEnvio) {
    return this.http.patch<Envio>(`${this.url}/${id}/estado`, { nuevoEstado });
  }
}

export function mensajeError(error: HttpErrorResponse): string {
  if (error.status === 0) return 'No se pudo conectar con el servidor. Verifica el backend y CORS.';
  if (error.status === 401) return 'Inicia sesión nuevamente para continuar.';
  if (error.status === 403) return 'Tu usuario no tiene permiso para esta acción. Verifica tu sesión.';
  return error.error?.detail || 'No se pudo completar la operación. Verifica los datos.';
}
